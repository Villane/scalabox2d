package org.villane.box2d.dynamics

import collision._
import joints._
import contacts._
import vecmath._
import vecmath.Preamble._
import settings.Settings
import common._

import collection.jcl.ArrayList

/**
 * Handles much of the heavy lifting of physics solving - for internal use.
 */
class Island(val bodyCapacity: Int,
             val contactCapacity: Int,
             val jointCapacity: Int,
             val listener: ContactListener) {
  // XXX To reduce code size, replaced the three fixed-size arrays with listbuffers. Perhaps should return later?
  val bodies = new ArrayList[Body]//(new java.util.ArrayList[Body](bodyCapacity))
  var contacts = new DLLHead[Contact](null) //(new java.util.ArrayList[Contact](contactCapacity))
  var joints = new ArrayList[Joint]//(new java.util.ArrayList[Joint](jointCapacity))

  // ERKKI: This was static, but that seemed like a bug
  var positionIterationCount = 0

  var positionError = 0f

  def clear() {
    bodies.clear()
    contacts.clear()
    joints.clear()
  }

  def add(body: Body) = bodies += body
  def add(contact: Contact) = contacts.add(contact.inIsland)//contacts += contact
  def add(joint: Joint) = joints += joint

  def solve(step: TimeStep, gravity: Vector2f, correctPositions: Boolean, allowSleep: Boolean) {
    // Integrate velocities and apply damping.
    for (b <- bodies) {
      if (!b.isStatic) {
      // Integrate velocities.
      b.linearVelocity += (gravity + b.force * b.invMass) * step.dt
      b.angularVelocity += step.dt * b.invI * b.torque

      // Reset forces.
      b.force = Vector2f.Zero
      b.torque = 0f

      // Apply damping.
      // ODE: dv/dt + c * v = 0
      // Solution: v(t) = v0 * exp(-c * t)
      // Time step: v(t + dt) = v0 * exp(-c * (t + dt)) = v0 * exp(-c * t) * exp(-c * dt) = v * exp(-c * dt)
      // v2 = exp(-c * dt) * v1
      // Taylor expansion:
      // v2 = (1.0f - c * dt) * v1
      b.linearVelocity *= (1f - step.dt * b.linearDamping).clamp(0f, 1f)
      b.angularVelocity *= (1f - step.dt * b.angularDamping).clamp(0f, 1f)

      // Check for large velocities.
      if ((b.linearVelocity ∙ b.linearVelocity) > Settings.maxLinearVelocitySquared) {
        b.linearVelocity = b.linearVelocity.normalize * Settings.maxLinearVelocity
      }

      if (b.angularVelocity * b.angularVelocity > Settings.maxAngularVelocitySquared) {
        if (b.angularVelocity < 0f) {
          b.angularVelocity = -Settings.maxAngularVelocity
        } else {
          b.angularVelocity = Settings.maxAngularVelocity
        }
      }
      }
    }

    val contactSolver = new ContactSolver(contacts.elements)

    // Initialize velocity constraints.
    contactSolver.initVelocityConstraints(step)

    for (joint <- joints) {
      joint.initVelocityConstraints(step)
    }

    // Solve velocity constraints.
    var i = 0
    while (i < step.maxIterations) {
      i += 1

      contactSolver.solveVelocityConstraints()

      var j = 0
      while (j < joints.length) {
        val joint = joints(j)
        j += 1
        
        joint.solveVelocityConstraints(step)
      }
    }

    // Post-solve (store impulses for warm starting).
    contactSolver.finalizeVelocityConstraints()

    // Integrate positions.
    for (b <- bodies) {
      if (!b.isStatic) {
      // Store positions for continuous collision.
      // TODO ERKKI : perhaps this should be capsulated as a method in body or sweep?
      b.sweep.c0 = b.sweep.c
      b.sweep.a0 = b.sweep.a

      // Integrate
      b.sweep.c += step.dt * b.linearVelocity
      b.sweep.a += step.dt * b.angularVelocity

      // Compute new transform
      b.synchronizeTransform()

      // Note: shapes are synchronized later.
      }
	}

    if (correctPositions) {
      // Initialize position constraints.
      // Contacts don't need initialization.
      for (joint <- joints) {
        joint.initPositionConstraints()
      }

      // Iterate over constraints.
      var loop = true
      positionIterationCount = 0
      while (loop && positionIterationCount < step.maxIterations) {
        val contactsOkay = contactSolver.solvePositionConstraints(Settings.contactBaumgarte)

        var jointsOkay = true
        // do we have to call this for all joints always? otherwise we could do
        // val jointsOkay = joints.forall(_.solvePositionConstraint())
        for (joint <- joints) {
          val jointOkay = joint.solvePositionConstraints() 
          jointsOkay &&= jointOkay
        }

        if (contactsOkay && jointsOkay) {
          loop = false
        } else {
          positionIterationCount += 1
        }
      }
    }

    report(contactSolver.constraints)

    if (allowSleep) {
      var minSleepTime = Float.MaxValue

      val linTolSqr = Settings.linearSleepTolerance * Settings.linearSleepTolerance
      val angTolSqr = Settings.angularSleepTolerance * Settings.angularSleepTolerance

      for (b <- bodies) {
        if (b.invMass != 0f) {
        if (! b.isAllowSleeping) {
          b.sleepTime = 0f
          minSleepTime = 0f
        }

        if (! b.isAllowSleeping ||
              b.angularVelocity * b.angularVelocity > angTolSqr ||
              (b.linearVelocity ∙ b.linearVelocity) > linTolSqr) {
          b.sleepTime = 0f
          minSleepTime = 0f
        } else {
          b.sleepTime += step.dt
          minSleepTime = MathUtil.min(minSleepTime, b.sleepTime)
        }
        }
      }

      if (minSleepTime >= Settings.timeToSleep) {
        for (b <- bodies) {
          b.flags |= BodyFlags.sleep
          b.linearVelocity = Vector2f.Zero
          b.angularVelocity = 0f
        }
      }
    }
  }

  def solveTOI(subStep: TimeStep) {
    val contactSolver = new ContactSolver(contacts.elements)

    // No warm starting needed for TOI events.

    // Solve velocity constraints.
    for (i <- 0 until subStep.maxIterations) {
      contactSolver.solveVelocityConstraints()
    }

    // Don't store the TOI contact forces for warm starting
    // because they can be quite large.

    // Integrate positions.
    for (b <- bodies) {
      if (!b.isStatic) {
      //System.out.println("(Island::SolveTOI 1) :"+b.m_sweep);
      // Store positions for continuous collision.
      b.sweep.c0 = b.sweep.c
      b.sweep.a0 = b.sweep.a

      // Integrate
      b.sweep.c += subStep.dt * b.linearVelocity
      b.sweep.a += subStep.dt * b.angularVelocity

      //System.out.println("(Island::SolveTOI 2) :"+b.m_sweep);
      // Compute new transform
      b.synchronizeTransform();

      //	System.out.println("(Island::SolveTOI 3) :"+b.m_sweep);
      // Note: shapes are synchronized later.
      }
    }

    // Solve position constraints.
    val k_toiBaumgarte = 0.75f
    var contactsOkay = false
    var i = 0
    while (!contactsOkay && i < subStep.maxIterations) {
      val contactsOkay = contactSolver.solvePositionConstraints(k_toiBaumgarte)
      i += 1
    }

    report(contactSolver.constraints)
  }

  def report(constraints: Seq[ContactConstraint]): Unit = {
    if (listener == null) {
      return
    }

    val cIter = contacts.elements
    var i = 0
    while (cIter.hasNext) {
      val c = cIter.next
      val cc = constraints(i)
      val shape1 = c.shape1
      val shape2 = c.shape2
      val b1 = shape1.body
      for (manifold <- c.manifolds) {
        for (k <- 0 until manifold.points.length) {
          val point = manifold.points(k)
          val ccp = cc.points(k)
          val pos = b1.transform * point.localPoint1

          // TOI constraint results are not stored, so get
          // the result from the constraint.
          val id = new ContactID(point.id.features)
          val cr = new ContactResult(shape1, shape2,
                                     pos, manifold.normal,
                                     ccp.normalImpulse, ccp.tangentImpulse, id)
          listener.result(cr)
        }
      }
      i += 1
    }
  }
}
