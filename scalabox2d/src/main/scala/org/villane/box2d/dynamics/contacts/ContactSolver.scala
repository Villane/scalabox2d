package org.villane.box2d.dynamics.contacts

import dynamics._
import Settings.ε
import vecmath._
import vecmath.Preamble._

class ContactSolver(contacts: Seq[Contact]) {
  val constraints = initConstraints(contacts)
  
  @inline def initConstraints(contacts: Seq[Contact]) = {
    val tmpConstraints = new Array[ContactConstraint](contacts.length)

    var iContact = 0
    while (iContact < contacts.length) {
      val contact = contacts(iContact)

      assert(contact.solid)
      val b1 = contact.fixture1.body
      val b2 = contact.fixture2.body
      val manifolds = contact.manifolds
      val friction = contact.friction
      val restitution = contact.restitution

      val v1 = b1.linearVelocity
      val v2 = b2.linearVelocity
      val w1 = b1.angularVelocity
      val w2 = b2.angularVelocity

      var iMF = 0
      while (iMF < manifolds.length) {
        val manifold = manifolds(iMF)
        iMF += 1

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

        var k = 0
        while (k < c.points.length) {
          val cp = manifold.points(k)
          val ccp = new ContactConstraintPoint
          c.points(k) = ccp

          k += 1
          
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

          //val tangent = normal.normal
          val tangentx = normal.y
          val tangenty = -normal.x

          //var rt1 = ccp.r1 × tangent
          //var rt2 = ccp.r2 × tangent
          var rt1 = ccp.r1.x * tangenty - ccp.r1.y * tangentx
          var rt2 = ccp.r2.x * tangenty - ccp.r2.y * tangentx
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

        tmpConstraints(iContact) = c
        iContact += 1
      }
    }

    tmpConstraints
  }
  
  @inline def initVelocityConstraints(step: TimeStep) {
    // Zero temp objects created - ewjordan

    // Warm start.
    var iConstraints = 0
    while (iConstraints < constraints.length) {
      val c = constraints(iConstraints)
      iConstraints += 1

      val b1 = c.body1
      val b2 = c.body2
      val invMass1 = b1.invMass
      val invI1 = b1.invI
      val invMass2 = b2.invMass
      val invI2 = b2.invI
      val normal = c.normal
      //val tangent = normal.normal
      val tangentx = normal.y
      val tangenty = -normal.x

      if (step.warmStarting) {
        var iCCP = 0
        while (iCCP < c.points.length) {
          val ccp = c.points(iCCP)
          iCCP += 1

          //Inlined all vector ops here
          ccp.normalImpulse *= step.dtRatio
          ccp.tangentImpulse *= step.dtRatio

          //val p = ccp.normalImpulse * normal + ccp.tangentImpulse * tangent
          val px = ccp.normalImpulse * normal.x + ccp.tangentImpulse * tangentx
          val py = ccp.normalImpulse * normal.y + ccp.tangentImpulse * tangenty

          b1.angularVelocity -= invI1 * (ccp.r1.x * py - ccp.r1.y * px)
          b1.linearVelocity -= Vector2(px * invMass1, py * invMass1)
          b2.angularVelocity += invI2 * (ccp.r2.x * py - ccp.r2.y * px)
          b2.linearVelocity += Vector2(px * invMass2, py * invMass2)
        }
      } else {
        var iCCP = 0
        while (iCCP < c.points.length) {
          val ccp = c.points(iCCP)
          iCCP += 1

          ccp.normalImpulse = 0f
          ccp.tangentImpulse = 0f
        }
      }
    }
  }
  
  @inline def solveVelocityConstraints() {
    // (4*constraints + 6*points) temp Vec2s - BOTTLENECK!
    var iConstraints = 0
    while (iConstraints < constraints.length) {
      val c = constraints(iConstraints)
      iConstraints += 1

      val b1 = c.body1
      val b2 = c.body2
      var w1 = b1.angularVelocity
      var w2 = b2.angularVelocity
      var v1x = b1.linearVelocity.x
      var v1y = b1.linearVelocity.y
      var v2x = b2.linearVelocity.x
      var v2y = b2.linearVelocity.y
      val invMass1 = b1.invMass
      val invI1 = b1.invI
      val invMass2 = b2.invMass
      val invI2 = b2.invI
      val normal = c.normal
      //val tangent = normal.normal
      val tangentx = normal.y
      val tangenty = -normal.x
      val friction = c.friction
            
            //final boolean DEFERRED_UPDATE = false;
            //if (DEFERRED_UPDATE) {
//            		Vec2 b1_linearVelocity = b1.m_linearVelocity.clone();
//            		float b1_angularVelocity = b1.m_angularVelocity;
//            		Vec2 b2_linearVelocity = b2.m_linearVelocity.clone();
//            		float b2_angularVelocity = b2.m_angularVelocity;
            //}
            
      // Solver normal constraints
      var iCCP = 0
      while (iCCP < c.points.length) {
        val ccp = c.points(iCCP)
        iCCP += 1
        
        // Relative velocity at contact
        //Vec2 dv = v2.add((w2 × ccp.r2));
        //dv.subLocal(v1);
        //Vec2 a = ccp.r1;
        //dv.subLocal(new Vec2(-w1 * a.y, w1 * a.x));
        val dvx = v2x - w2 * ccp.r2.y - v1x + w1*ccp.r1.y
        val dvy = v2y + w2 * ccp.r2.x - v1y - w1*ccp.r1.x

        // Compute normal impulse
        val vn = dvx*normal.x + dvy*normal.y;//(dv ∙ normal)
        var λ = - ccp.normalMass * (vn - ccp.velocityBias)

        // b2Clamp the accumulated force
        val newImpulse = max(ccp.normalImpulse + λ, 0f)
        λ = newImpulse - ccp.normalImpulse

        // Apply contact impulse
        //val P = normal * λ
        val Px = normal.x * λ
        val Py = normal.y * λ
        v1x -= Px * invMass1
        v1y -= Py * invMass1
        w1 -= invI1 * (ccp.r1.x * Py - ccp.r1.y * Px) //(ccp.r1 × P);

        v2x += Px * invMass2
        v2y += Py * invMass2
        w2 += invI2 * (ccp.r2.x * Py - ccp.r2.y * Px) //(ccp.r2 × P);

        ccp.normalImpulse = newImpulse;
      }
            
//            //#ifdef DEFERRED_UPDATE
//    		b1.m_linearVelocity = b1_linearVelocity;
//    		b1.m_angularVelocity = b1_angularVelocity;
//    		b2.m_linearVelocity = b2_linearVelocity;
//    		b2.m_angularVelocity = b2_angularVelocity;
//    		// #endif

      // Solver tangent constraints
      iCCP = 0
      while (iCCP < c.points.length) {
        val ccp = c.points(iCCP)
        iCCP += 1

        // Relative velocity at contact
        //Vec2 dv = v2.add((w2 × ccp.r2));
        //dv.subLocal(v1);
        //dv.subLocal((w1 × ccp.r1));
        val dvx = v2x - w2 * ccp.r2.y - v1x + w1*ccp.r1.y
        val dvy = v2y + w2 * ccp.r2.x - v1y - w1*ccp.r1.x

        // Compute tangent force
        val vt = dvx * tangentx + dvy * tangenty
        var λ = ccp.tangentMass * (-vt)

        // b2Clamp the accumulated force
        val maxFriction = friction * ccp.normalImpulse
        val newImpulse = clamp(ccp.tangentImpulse + λ, -maxFriction, maxFriction)
        λ = newImpulse - ccp.tangentImpulse

        // Apply contact impulse
        //val P = tangent * λ
        val Px = tangentx * λ 
        val Py = tangenty * λ 

        // b1.m_linearVelocity.subLocal(P.mul(invMass1));
        v1x -= Px * invMass1
        v1y -= Py * invMass1
        // b1.m_angularVelocity -= invI1 * (r1 × P);
        w1 -= invI1 * (ccp.r1.x * Py - ccp.r1.y * Px)

        // b2.m_linearVelocity.addLocal(P.mul(invMass2));
        v2x += Px * invMass2
        v2y += Py * invMass2
        // b2.m_angularVelocity += invI2 * (r2 × P);
        w2 += invI2 * (ccp.r2.x * Py - ccp.r2.y * Px)

        ccp.tangentImpulse = newImpulse
      }
      b1.linearVelocity = Vector2(v1x, v1y)
      b1.angularVelocity = w1
      b2.linearVelocity = Vector2(v2x, v2y)
      b2.angularVelocity = w2
    }
  }

  @inline def finalizeVelocityConstraints() {
    var ic = 0
    while (ic < constraints.length) {
      val c = constraints(ic)
      ic += 1

      val m = c.manifold
      var j = 0
      while (j < c.points.length) {
        m.points(j).normalImpulse = c.points(j).normalImpulse
        m.points(j).tangentImpulse = c.points(j).tangentImpulse
        j += 1
      }
    }
  }
  
  @inline def solvePositionConstraints(baumgarte: Float): Boolean = {
    var minSeparation = 0f
    var iConstraints = 0
    while (iConstraints < constraints.length) {
      val c = constraints(iConstraints)
      iConstraints += 1

      val b1 = c.body1
      val b2 = c.body2
      val invMass1 = b1.mass * b1.invMass
      val invI1 = b1.mass * b1.invI
      val invMass2 = b2.mass * b2.invMass
      val invI2 = b2.mass * b2.invI

	  val normal = c.normal

      // Solver normal constraints
      var iCCP = 0 
      while (iCCP < c.points.length) {
        val ccp = c.points(iCCP)
        iCCP += 1

        //val r1 = b1.transform.rot * (ccp.localAnchor1 - b1.localCenter)
        //val r2 = b2.transform.rot * (ccp.localAnchor2 - b2.localCenter)
        // matrix * v, x = col1.x * v.x + col2.x * v.y
        // matrix * v, y = col1.y * v.x + col2.y * v.y
        val r1x2 = ccp.localAnchor1.x - b1.localCenter.x
        val r1y2 = ccp.localAnchor1.y - b1.localCenter.y
        val r1x = b1.transform.rot.a11 * r1x2 + b1.transform.rot.a12 * r1y2 
        val r1y = b1.transform.rot.a21 * r1x2 + b1.transform.rot.a22 * r1y2 
        val r2x2 = ccp.localAnchor2.x - b1.localCenter.x
        val r2y2 = ccp.localAnchor2.y - b1.localCenter.y
        val r2x = b2.transform.rot.a11 * r2x2 + b2.transform.rot.a12 * r2y2 
        val r2y = b2.transform.rot.a21 * r2x2 + b2.transform.rot.a22 * r2y2 

        //Vec2 p1 = b1.m_sweep.c + r1;
        //Vec2 p2 = b2.m_sweep.c + r2;
        //Vec2 dp = p2 - p1;
        val dpx = b2.sweep.c.x + r2x - b1.sweep.c.x - r1x
        val dpy = b2.sweep.c.y + r2y - b1.sweep.c.y - r1y


        // Approximate the current separation.
        val separation = dpx*normal.x + dpy*normal.y + ccp.separation;//(dp ∙ normal) + ccp->separation;

        // Track max constraint error.
        minSeparation = min(minSeparation, separation)

        // Prevent large corrections and allow slop.
        val C = baumgarte * clamp(separation + Settings.linearSlop, -Settings.maxLinearCorrection, 0f)

        // Compute normal impulse
        var dImpulse = -ccp.equalizedMass * C

        // b2Clamp the accumulated impulse
        val impulse0 = ccp.positionImpulse
        ccp.positionImpulse = max(impulse0 + dImpulse, 0f)
        dImpulse = ccp.positionImpulse - impulse0

        //val impulse = normal * dImpulse
        val impulsex = normal.x * dImpulse
        val impulsey = normal.y * dImpulse

        //b1.sweep.c -= impulse * invMass1
        b1.sweep.c -= Vector2(impulsex * invMass1, impulsey * invMass1)
        b1.sweep.a -= invI1 * (r1x*impulsey - r1y*impulsex)//(r1 × impulse);
        b1.synchronizeTransform()

        //b2.sweep.c += impulse * invMass2 
        b2.sweep.c += Vector2(impulsex * invMass2, impulsey * invMass2)
        b2.sweep.a += invI2 * (r2x*impulsey - r2y*impulsex)//(r2 × impulse);
        b2.synchronizeTransform()
      }
    }

    // We can't expect minSpeparation >= -b2_linearSlop because we don't
    // push the separation above -b2_linearSlop.
    minSeparation >= -1.5f * Settings.linearSlop
  }
}
