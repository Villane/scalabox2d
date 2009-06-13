package org.villane.box2d.dynamics.joints

import vecmath._
import vecmath.Preamble._
import Settings.ε

//Linear constraint (point-to-line)
//d = p2 - p1 = x2 + r2 - x1 - r1
//C = dot(ay1, d)
//Cdot = dot(d, cross(w1, ay1)) + dot(ay1, v2 + cross(w2, r2) - v1 - cross(w1, r1))
//   = -dot(ay1, v1) - dot(cross(d + r1, ay1), w1) + dot(ay1, v2) + dot(cross(r2, ay1), v2)
//J = [-ay1 -cross(d+r1,ay1) ay1 cross(r2,ay1)]
//
//Angular constraint
//C = a2 - a1 + a_initial
//Cdot = w2 - w1
//J = [0 0 -1 0 0 1]

/**
 * A prismatic joint. This joint provides one degree of freedom: translation
 * along an axis fixed in body1. Relative rotation is prevented. You can
 * use a joint limit to restrict the range of motion and a joint motor to
 * drive the motion or to model joint friction.
 */
class PrismaticJoint(defn: PrismaticJointDef) extends Joint(defn) {
  val localAnchor1 = defn.localAnchor1 // relative
  val localAnchor2 = defn.localAnchor2
  val localXAxis1 = defn.localAxis1
  val localYAxis1 = 1f × defn.localAxis1
  val referenceAngle = defn.referenceAngle

  var linearJacobian = Jacobian.Zero
  var linearMass = 0f
  var force = 0f

  var angularMass = 0f // effective mass for angular constraint.
  var torque = 0f

  var motorJacobian = Jacobian.Zero
  var motorMass = 0f // effective mass for motor/limit translational constraint.
  var motorForce = 0f
  var limitForce = 0f
  var limitPositionImpulse = 0f

  var lowerTranslation = defn.lowerTranslation
  var upperTranslation = defn.upperTranslation
  val maxMotorForce = defn.maxMotorForce
  var motorSpeed = defn.motorSpeed 

  val enableMotor = defn.enableMotor
  val enableLimit = defn.enableLimit
  var limitState: LimitState.Value = null

  def initVelocityConstraints(step: TimeStep) {
    val b1 = body1
    val b2 = body2

    // Compute the effective masses.
    val r1 = b1.transform.rot * (localAnchor1 - b1.localCenter)
	val r2 = b2.transform.rot * (localAnchor2 - b2.localCenter)

    val invMass1 = b1.invMass
    val invMass2 = b2.invMass
    val invI1 = b1.invI
    val invI2 = b2.invI

    // Compute point to line constraint effective mass.
    // J = [-ay1 -cross(d+r1,ay1) ay1 cross(r2,ay1)]
    val ay1 = b1.transform.rot * localYAxis1
    val e = (b2.sweep.c + r2) - b1.sweep.c // e = d + r1

    linearJacobian = Jacobian(-ay1, -(e × ay1), ay1, r2 × ay1)
    linearMass = invMass1 + invI1 * linearJacobian.angular1 *
                linearJacobian.angular1 + invMass2 + invI2 *
                linearJacobian.angular2 * linearJacobian.angular2

    assert(linearMass > ε)

    linearMass = 1.0f / linearMass

    // Compute angular constraint effective mass.
    angularMass = invI1 + invI2
    if (angularMass > ε) {
      angularMass = 1.0f / angularMass
    }

    // Compute motor and limit terms.
    if (enableLimit || enableMotor) {
      // The motor and limit share a Jacobian and effective mass.
      val ax1 = b1.transform.rot * localXAxis1

      motorJacobian = Jacobian(-ax1, -(e × ax1), ax1, r2 × ax1)
      motorMass = invMass1 + invI1 * motorJacobian.angular1 *
                    motorJacobian.angular1 + invMass2 + invI2 *
                    motorJacobian.angular2 * motorJacobian.angular2
      assert (motorMass > ε)
      motorMass = 1.0f / motorMass

      if (enableLimit) {
        val d = e - r1 // p2 - p1
        val jointTranslation = ax1 ∙ d

        if ((upperTranslation - lowerTranslation).abs < 2.0f * Settings.linearSlop) {
          limitState = LimitState.EQUAL_LIMITS
        } else if (jointTranslation <= lowerTranslation) {
          if (limitState != LimitState.AT_LOWER_LIMIT) {
            limitForce = 0f
          }
          limitState = LimitState.AT_LOWER_LIMIT
        } else if (jointTranslation >= upperTranslation) {
          if (limitState != LimitState.AT_UPPER_LIMIT) {
            limitForce = 0f
          }
          limitState = LimitState.AT_UPPER_LIMIT
        } else {
          limitState = LimitState.INACTIVE_LIMIT
          limitForce = 0f
        }
      }
    }

    if (!enableMotor) {
      motorForce = 0f
    }

    if (!enableLimit) {
      limitForce = 0f
    }

    if (step.warmStarting) {
      val P1 = Vector2( step.dt * (force * linearJacobian.linear1.x + (motorForce + limitForce) * motorJacobian.linear1.x),
    							step.dt * (force * linearJacobian.linear1.y + (motorForce + limitForce) * motorJacobian.linear1.y) )
      val P2 = Vector2( step.dt * (force * linearJacobian.linear2.x + (motorForce + limitForce) * motorJacobian.linear2.x),
    							step.dt * (force * linearJacobian.linear2.y + (motorForce + limitForce) * motorJacobian.linear2.y) )
      val L1 = step.dt * (force * linearJacobian.angular1 - torque + (motorForce + limitForce) * motorJacobian.angular1)
      val L2 = step.dt * (force * linearJacobian.angular2 + torque + (motorForce + limitForce) * motorJacobian.angular2)

      b1.linearVelocity += invMass1 * P1
      b1.angularVelocity += invI1 * L1

      b2.linearVelocity += invMass2 * P2
      b2.angularVelocity += invI2 * L2  
    } else {
      force = 0.0f;
      torque = 0.0f;
      limitForce = 0.0f;
      motorForce = 0.0f;
    }
    limitPositionImpulse = 0.0f;
  }

  def solveVelocityConstraints(step: TimeStep) {
    val b1 = body1
    val b2 = body2

    val invMass1 = b1.invMass
    val invMass2 = b2.invMass
    val invI1 = b1.invI
    val invI2 = b2.invI

    // Solve linear constraint.
    val linearCdot = linearJacobian.compute(b1.linearVelocity, b1.angularVelocity,
                        b2.linearVelocity, b2.angularVelocity)
    val f = -step.invDt * linearMass * linearCdot
    force += f

    val P = step.dt * f
    b1.linearVelocity += (invMass1 * P) * linearJacobian.linear1
    b1.angularVelocity += invI1 * P * linearJacobian.angular1

    b2.linearVelocity += (invMass2 * P) * linearJacobian.linear2
    b2.angularVelocity += invI2 * P * linearJacobian.angular2

    // Solve angular constraint.
    val angularCdot = b2.angularVelocity - b1.angularVelocity
    val t = -step.invDt * angularMass * angularCdot
    torque += t

    val L = step.dt * t
    b1.angularVelocity -= invI1 * L
    b2.angularVelocity += invI2 * L

    // Solve linear motor constraint.
    if (enableMotor && limitState != LimitState.EQUAL_LIMITS) {
      val motorCdot = motorJacobian.compute(b1.linearVelocity, b1.angularVelocity, b2.linearVelocity, b2.angularVelocity) - motorSpeed
      var motorF = -step.invDt * motorMass * motorCdot
      val oldMotorForce = motorForce
      motorForce = clamp(motorForce + motorF, -maxMotorForce, maxMotorForce)
      motorF = motorForce - oldMotorForce

      val P2 = step.dt * motorF
      b1.linearVelocity += (invMass1 * P2) * motorJacobian.linear1
      b1.angularVelocity += invI1 * P2 * motorJacobian.angular1

      b2.linearVelocity += (invMass2 * P2) * motorJacobian.linear2
      b2.angularVelocity += invI2 * P2 * motorJacobian.angular2
    }

    // Solve linear limit constraint.
    if (enableLimit && limitState != LimitState.INACTIVE_LIMIT) {
      val limitCdot = motorJacobian.compute(b1.linearVelocity, b1.angularVelocity, b2.linearVelocity, b2.angularVelocity)
      var limitF = -step.invDt * motorMass * limitCdot

      if (limitState == LimitState.EQUAL_LIMITS) {
        limitForce += limitF
      } else if (limitState == LimitState.AT_LOWER_LIMIT) {
        val oldLimitForce = limitForce
        limitForce = max(limitForce + limitF, 0.0f)
        limitF = limitForce - oldLimitForce
      } else if (limitState == LimitState.AT_UPPER_LIMIT) {
        val oldLimitForce = limitForce
        limitForce = min(limitForce + limitF, 0.0f)
        limitF = limitForce - oldLimitForce
      }

      val P2 = step.dt * limitF

      b1.linearVelocity += (invMass1 * P2) * motorJacobian.linear1
      b1.angularVelocity += invI1 * P2 * motorJacobian.angular1

      b2.linearVelocity += (invMass2 * P2) * motorJacobian.linear2
      b2.angularVelocity += invI2 * P2 * motorJacobian.angular2
    }
  }

  def solvePositionConstraints() = {
    val b1 = body1
    val b2 = body2

    val invMass1 = b1.invMass
    val invMass2 = b2.invMass
    val invI1 = b1.invI
    val invI2 = b2.invI

    val r1 = b1.transform.rot * (localAnchor1 - b1.localCenter)
	val r2 = b2.transform.rot * (localAnchor2 - b2.localCenter)
    val p1 = b1.sweep.c + r1
    val p2 = b2.sweep.c + r2
    val d = p2 - p1
    val ay1 = b1.transform.rot * localYAxis1

    // Solve linear (point-to-line) constraint.
    var linearC = ay1 ∙ d
    // Prevent overly large corrections.
    linearC = clamp(linearC, -Settings.maxLinearCorrection, Settings.maxLinearCorrection)
    val linearImpulse = -linearMass * linearC

    b1.sweep.c += (invMass1 * linearImpulse) * linearJacobian.linear1
    b1.sweep.a += invI1 * linearImpulse * linearJacobian.angular1
    //b1.SynchronizeTransform(); // updated by angular constraint
    b2.sweep.c += (invMass2 * linearImpulse) * linearJacobian.linear2
    b2.sweep.a += invI2 * linearImpulse * linearJacobian.angular2
    //b2.SynchronizeTransform(); // updated by angular constraint

    var positionError = linearC.abs

    // Solve angular constraint.
    var angularC = b2.sweep.a - b1.sweep.a - referenceAngle
    // Prevent overly large corrections.
    angularC = clamp(angularC, -Settings.maxAngularCorrection, Settings.maxAngularCorrection)
    val angularImpulse = -angularMass * angularC

    b1.sweep.a -= b1.invI * angularImpulse
    b2.sweep.a += b2.invI * angularImpulse

    b1.synchronizeTransform
    b2.synchronizeTransform

    var angularError = angularC.abs

    // Solve linear limit constraint.
    if (enableLimit && limitState != LimitState.INACTIVE_LIMIT) {
      val r1z = b1.transform.rot * (localAnchor1 - b1.localCenter)
      val r2z = b2.transform.rot * (localAnchor2 - b2.localCenter)
      val p1z = b1.sweep.c + r1z
      val p2z = b2.sweep.c + r2z
      val dz = p2z - p1z
      val ax1 = b1.transform.rot * localXAxis1

      val translation = ax1 dot dz
      var limitImpulse = 0f

      if (limitState == LimitState.EQUAL_LIMITS) {
        // Prevent large angular corrections
        val limitC = clamp(translation, -Settings.maxLinearCorrection, Settings.maxLinearCorrection)
        limitImpulse = -motorMass * limitC
        positionError = max(positionError, angularC.abs)
      } else if (limitState == LimitState.AT_LOWER_LIMIT) {
        var limitC = translation - lowerTranslation
        positionError = max(positionError, -limitC)

        // Prevent large linear corrections and allow some slop.
        limitC = clamp(limitC + Settings.linearSlop, -Settings.maxLinearCorrection, 0.0f)
        limitImpulse = -motorMass * limitC
        val oldLimitImpulse = limitPositionImpulse
        limitPositionImpulse = max(limitPositionImpulse + limitImpulse, 0.0f)
        limitImpulse = limitPositionImpulse - oldLimitImpulse
      } else if (limitState == LimitState.AT_UPPER_LIMIT) {
        var limitC = translation - upperTranslation
        positionError = max(positionError, limitC)

        // Prevent large linear corrections and allow some slop.
        limitC = clamp(limitC - Settings.linearSlop, 0.0f, Settings.maxLinearCorrection)
        limitImpulse = -motorMass * limitC
        val oldLimitImpulse = limitPositionImpulse
        limitPositionImpulse = min(limitPositionImpulse + limitImpulse, 0.0f)
        limitImpulse = limitPositionImpulse - oldLimitImpulse
      }

      b1.sweep.c += (invMass1 * limitImpulse) * motorJacobian.linear1
      b1.sweep.a += invI1 * limitImpulse * motorJacobian.angular1
      b2.sweep.c += (invMass2 * limitImpulse) * motorJacobian.linear2
      b2.sweep.a += invI2 * limitImpulse * motorJacobian.angular2

      b1.synchronizeTransform
      b2.synchronizeTransform
    }

    positionError <= Settings.linearSlop && angularError <= Settings.angularSlop
  }

  def anchor1 = body1.toWorldPoint(localAnchor1)

  def anchor2 = body2.toWorldPoint(localAnchor2)

  /// Get the current joint translation, usually in meters.
  def jointTranslation = {
    val b1 = body1
    val b2 = body2

    val p1 = b1.toWorldPoint(localAnchor1)
    val p2 = b2.toWorldPoint(localAnchor2)
    val d = p2 - p1
    val axis = b1.toWorldVector(localXAxis1)

    d ∙ axis
  }

  /// Get the current joint translation speed, usually in meters per second.
  def jointSpeed = {
    val b1 = body1
    val b2 = body2

    val r1 = b1.transform.rot * (localAnchor1 - b1.localCenter)
    val r2 = b2.transform.rot * (localAnchor2 - b2.localCenter)
    val p1 = b1.sweep.c + r1
    val p2 = b2.sweep.c + r2
    val d = p2 - p1
    val axis = b1.toWorldVector(localXAxis1)

    val v1 = b1.linearVelocity
	val v2 = b2.linearVelocity
    val w1 = b1.angularVelocity
    val w2 = b2.angularVelocity

    (d ∙ (w1 × axis)) + (axis ∙ ((v2 + (w2 × r2)) - v1 - (w1 × r1)))
  }

  def reactionForce = {
    val ax1 = body1.transform.rot * localXAxis1
    val ay1 = body1.transform.rot * localYAxis1
    Vector2(limitForce * ax1.x + force * ay1.x,
             limitForce * ax1.y + force * ay1.y)
  }

  def reactionTorque = torque

  /** Set the joint limits, usually in meters. */
  def setLimits(lower: Float, upper: Float) {
    assert(lower <= upper)
    lowerTranslation = lower
    upperTranslation = upper
  }
}
