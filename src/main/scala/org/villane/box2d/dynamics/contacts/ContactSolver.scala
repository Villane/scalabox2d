package org.villane.box2d.dynamics.contacts

import dynamics._
import settings.Settings
import settings.Settings.ε
import vecmath._
import vecmath.Preamble._

class ContactSolver(contacts: Seq[Contact]) {
  val constraints = initConstraints(contacts)
  
  def initConstraints(contacts: Seq[Contact]) = {
    val tmpConstraints = new collection.mutable.ArrayBuffer[ContactConstraint]

    for (contact <- contacts) {
      assert(contact.solid)
      val b1 = contact.shape1.body
      val b2 = contact.shape2.body
      val manifolds = contact.manifolds
      val friction = contact.friction
      val restitution = contact.restitution

      val v1 = b1.linearVelocity
      val v2 = b2.linearVelocity
      val w1 = b1.angularVelocity
      val w2 = b2.angularVelocity

      for (manifold <- manifolds) {
        assert (manifold.points.length > 0, "Manifold has length 0")
        val normal = manifold.normal

        val c = new ContactConstraint
        c.body1 = b1
        c.body2 = b2
        c.manifold = manifold
        c.normal = normal
        c.points = new Array[ContactConstraintPoint](manifold.points.length)
        c.friction = friction
        c.restitution = restitution

        for (k <- 0 until c.points.length) {
          val cp = manifold.points(k)
          val ccp = new ContactConstraintPoint
          c.points(k) = ccp

          ccp.normalImpulse = cp.normalImpulse
          ccp.tangentImpulse = cp.tangentImpulse
          ccp.separation = cp.separation
          ccp.positionImpulse = 0f

          ccp.localAnchor1 = cp.localPoint1
          ccp.localAnchor2 = cp.localPoint2
          ccp.r1 = b1.transform.rot * (cp.localPoint1 - b1.localCenter)
          ccp.r2 = b2.transform.rot * (cp.localPoint2 - b2.localCenter)

          var rn1 = ccp.r1 × normal
          var rn2 = ccp.r2 × normal
          rn1 *= rn1
          rn2 *= rn2

          val kNormal = b1.invMass + b2.invMass + b1.invI * rn1 + b2.invI * rn2

          
          assert (kNormal > ε)
          ccp.normalMass = 1 / kNormal

          var kEqualized = b1.mass * b1.invMass + b2.mass * b2.invMass
          kEqualized += b1.mass * b1.invI * rn1 + b2.mass * b2.invI * rn2

          assert(kEqualized > ε)
          ccp.equalizedMass = 1 / kEqualized

          val tangent = normal.tangent

          var rt1 = ccp.r1 × tangent
          var rt2 = ccp.r2 × tangent
          rt1 *= rt1
          rt2 *= rt2

          val kTangent = b1.invMass + b2.invMass + b1.invI * rt1 + b2.invI * rt2
                    
          assert (kTangent > ε)
          ccp.tangentMass = 1 / kTangent

          // Setup a velocity bias for restitution.
          ccp.velocityBias = 0f
          if (ccp.separation > 0.0f) {
            ccp.velocityBias = -60.0f * ccp.separation; // TODO_ERIN b2TimeStep
          }

          val buffer = w2 × ccp.r2 - w1 × ccp.r1 + v2 - v1
          val vRel = c.normal ∙ buffer
          
          if (vRel < -Settings.velocityThreshold) {
            ccp.velocityBias += -c.restitution * vRel
          }

        }

        tmpConstraints += c
      }
    }

    tmpConstraints.toArray
  }
  
  def initVelocityConstraints(step: TimeStep) {
    // Zero temp objects created - ewjordan

    // Warm start.
    for (c <- constraints) {
      val b1 = c.body1
      val b2 = c.body2
      val invMass1 = b1.invMass
      val invI1 = b1.invI
      val invMass2 = b2.invMass
      val invI2 = b2.invI
      val normal = c.normal
      val tangent = normal.tangent

      if (step.warmStarting) {
        for (ccp <- c.points) {
          //Inlined all vector ops here
          ccp.normalImpulse *= step.dtRatio
          ccp.tangentImpulse *= step.dtRatio

          val p = ccp.normalImpulse * normal + ccp.tangentImpulse * tangent

          b1.angularVelocity -= invI1 * (ccp.r1.x * p.y - ccp.r1.y * p.x)
          b1.linearVelocity -= p * invMass1
          b2.angularVelocity += invI2 * (ccp.r2.x * p.y - ccp.r2.y * p.x)
          b2.linearVelocity += p * invMass2
        }
      } else {
        for (ccp <- c.points) {
          ccp.normalImpulse = 0f
          ccp.tangentImpulse = 0f
        }
      }
    }
  }
  
  def solveVelocityConstraints() {
    // (4*constraints + 6*points) temp Vec2s - BOTTLENECK!
    for (c <- constraints) {
      val b1 = c.body1
      val b2 = c.body2
      var w1 = b1.angularVelocity
      var w2 = b2.angularVelocity
      var v1 = b1.linearVelocity
      var v2 = b2.linearVelocity
      val invMass1 = b1.invMass
      val invI1 = b1.invI
      val invMass2 = b2.invMass
      val invI2 = b2.invI
      val normal = c.normal
      val tangent = normal.tangent
      val friction = c.friction
            
            //final boolean DEFERRED_UPDATE = false;
            //if (DEFERRED_UPDATE) {
//            		Vec2 b1_linearVelocity = b1.m_linearVelocity.clone();
//            		float b1_angularVelocity = b1.m_angularVelocity;
//            		Vec2 b2_linearVelocity = b2.m_linearVelocity.clone();
//            		float b2_angularVelocity = b2.m_angularVelocity;
            //}
            
      // Solver normal constraints
      for (ccp <- c.points) {
        // Relative velocity at contact
        //Vec2 dv = v2.add((w2 × ccp.r2));
        //dv.subLocal(v1);
        //Vec2 a = ccp.r1;
        //dv.subLocal(new Vec2(-w1 * a.y, w1 * a.x));
        val dvx = v2.x - w2 * ccp.r2.y - v1.x + w1*ccp.r1.y
        val dvy = v2.y + w2 * ccp.r2.x - v1.y - w1*ccp.r1.x

        // Compute normal impulse
        val vn = dvx*normal.x + dvy*normal.y;//(dv ∙ normal)
        var λ = - ccp.normalMass * (vn - ccp.velocityBias)

        // b2Clamp the accumulated force
        val newImpulse = MathUtil.max(ccp.normalImpulse + λ, 0f)
        λ = newImpulse - ccp.normalImpulse

        // Apply contact impulse
        val P = normal * λ
        v1 -= P * invMass1
        w1 -= invI1 * (ccp.r1.x * P.y - ccp.r1.y * P.x) //(ccp.r1 × P);

        v2 += P * invMass2
        w2 += invI2 * (ccp.r2.x * P.y - ccp.r2.y * P.x) //(ccp.r2 × P);

        ccp.normalImpulse = newImpulse;
      }
            
//            //#ifdef DEFERRED_UPDATE
//    		b1.m_linearVelocity = b1_linearVelocity;
//    		b1.m_angularVelocity = b1_angularVelocity;
//    		b2.m_linearVelocity = b2_linearVelocity;
//    		b2.m_angularVelocity = b2_angularVelocity;
//    		// #endif

      // Solver tangent constraints
      for (ccp <- c.points) {
        // Relative velocity at contact
        //Vec2 dv = v2.add((w2 × ccp.r2));
        //dv.subLocal(v1);
        //dv.subLocal((w1 × ccp.r1));
        val dvx = v2.x - w2 * ccp.r2.y - v1.x + w1*ccp.r1.y
        val dvy = v2.y + w2 * ccp.r2.x - v1.y - w1*ccp.r1.x

        // Compute tangent force
        val vt = dvx * tangent.x + dvy * tangent.y
        var λ = ccp.tangentMass * (-vt)

        // b2Clamp the accumulated force
        val maxFriction = friction * ccp.normalImpulse
        val newImpulse = MathUtil.clamp(ccp.tangentImpulse + λ, -maxFriction, maxFriction)
        λ = newImpulse - ccp.tangentImpulse

        // Apply contact impulse
        val P = tangent * λ

        // b1.m_linearVelocity.subLocal(P.mul(invMass1));
        v1 -= P * invMass1
        // b1.m_angularVelocity -= invI1 * (r1 × P);
        w1 -= invI1 * (ccp.r1.x * P.y - ccp.r1.y * P.x)

        // b2.m_linearVelocity.addLocal(P.mul(invMass2));
        v2 += P * invMass2
        // b2.m_angularVelocity += invI2 * (r2 × P);
        w2 += invI2 * (ccp.r2.x * P.y - ccp.r2.y * P.x)

        ccp.tangentImpulse = newImpulse
      }
      b1.linearVelocity = v1
      b1.angularVelocity = w1
      b2.linearVelocity = v2
      b2.angularVelocity = w2
    }
  }
  
  def finalizeVelocityConstraints() {
    for (c <- constraints) {
      val m = c.manifold
      for (j <- 0 until c.points.length) {
        m.points(j).normalImpulse = c.points(j).normalImpulse
        m.points(j).tangentImpulse = c.points(j).tangentImpulse
      }
    }
  }
  
  def solvePositionConstraints(baumgarte: Float): Boolean = {
    var minSeparation = 0f
    for (c <- constraints) {
      val b1 = c.body1
      val b2 = c.body2
      val invMass1 = b1.mass * b1.invMass
      val invI1 = b1.mass * b1.invI
      val invMass2 = b2.mass * b2.invMass
      val invI2 = b2.mass * b2.invI

	  val normal = c.normal

      // Solver normal constraints
      for (ccp <- c.points) {
        val r1 = b1.transform.rot * (ccp.localAnchor1 - b1.localCenter)
        val r2 = b2.transform.rot * (ccp.localAnchor2 - b2.localCenter)

        //Vec2 p1 = b1.m_sweep.c + r1;
        //Vec2 p2 = b2.m_sweep.c + r2;
        //Vec2 dp = p2 - p1;
        val dpx = b2.sweep.c.x + r2.x - b1.sweep.c.x - r1.x
        val dpy = b2.sweep.c.y + r2.y - b1.sweep.c.y - r1.y


        // Approximate the current separation.
        val separation = dpx*normal.x + dpy*normal.y + ccp.separation;//(dp ∙ normal) + ccp->separation;

        // Track max constraint error.
        minSeparation = MathUtil.min(minSeparation, separation)

        // Prevent large corrections and allow slop.
        val C = baumgarte * MathUtil.clamp(separation + Settings.linearSlop, -Settings.maxLinearCorrection, 0f)

        // Compute normal impulse
        var dImpulse = -ccp.equalizedMass * C

        // b2Clamp the accumulated impulse
        val impulse0 = ccp.positionImpulse
        ccp.positionImpulse = MathUtil.max(impulse0 + dImpulse, 0f)
        dImpulse = ccp.positionImpulse - impulse0

        val impulse = normal * dImpulse

        b1.sweep.c -= impulse * invMass1
        b1.sweep.a -= invI1 * (r1.x*impulse.y - r1.y*impulse.x)//(r1 × impulse);
        b1.synchronizeTransform()

        b2.sweep.c += impulse * invMass2 
        b2.sweep.a += invI2 * (r2.x*impulse.y - r2.y*impulse.x)//(r2 × impulse);
        b2.synchronizeTransform()
      }
    }

    // We can't expect minSpeparation >= -b2_linearSlop because we don't
    // push the separation above -b2_linearSlop.
    minSeparation >= -1.5f * Settings.linearSlop
  }
}
