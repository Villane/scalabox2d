package org.villane.box2d.dynamics.joints

import vecmath._
import vecmath.Preamble._

object LimitState extends Enumeration {
  val INACTIVE_LIMIT, AT_LOWER_LIMIT, AT_UPPER_LIMIT, EQUAL_LIMITS = Value  
}

//Point-to-point constraint
//C = p2 - p1
//Cdot = v2 - v1
//   = v2 + cross(w2, r2) - v1 - cross(w1, r1)
//J = [-I -r1_skew I r2_skew ]
//Identity used:
//w k % (rx i + ry j) = w * (-ry i + rx j)

//Motor constraint
//Cdot = w2 - w1
//J = [0 0 -1 0 0 1]
//K = invI1 + invI2
class RevoluteJoint(defn: RevoluteJointDef) extends Joint(defn) {
  val localAnchor1 = defn.localAnchor1 // relative
  val localAnchor2 = defn.localAnchor2
  var pivotForce = Vector2f.Zero
  var motorForce = 0f
  var limitForce = 0f
  var limitPositionImpulse = 0f

  var pivotMass = Matrix2f.Zero // effective mass for point-to-point constraint.
  var motorMass = 0f // effective mass for motor/limit angular constraint.

  val enableMotor = defn.enableMotor
  val maxMotorTorque = defn.maxMotorTorque
  var motorSpeed = defn.motorSpeed 

  val enableLimit = defn.enableLimit
  val referenceAngle = defn.referenceAngle
  var lowerAngle = defn.lowerAngle
  var upperAngle = defn.upperAngle
  var limitState: LimitState.Value = null

  def initVelocityConstraints(step: TimeStep) {
    val b1 = body1
    val b2 = body2

    // Compute the effective mass matrix.
    val r1 = b1.transform.rot * (localAnchor1 - b1.localCenter)
    val r2 = b2.transform.rot * (localAnchor2 - b2.localCenter)

    // K    = [(1/m1 + 1/m2) * eye(2) - skew(r1) * invI1 * skew(r1) - skew(r2) * invI2 * skew(r2)]
    //      = [1/m1+1/m2     0    ] + invI1 * [r1.y*r1.y -r1.x*r1.y] + invI2 * [r1.y*r1.y -r1.x*r1.y]
    //        [    0     1/m1+1/m2]           [-r1.x*r1.y r1.x*r1.x]           [-r1.x*r1.y r1.x*r1.x]
    val invMass1 = b1.invMass
    val invMass2 = b2.invMass
    val invI1 = b1.invI
    val invI2 = b2.invI

    val K1 = Matrix2f(invMass1 + invMass2, 0f,
                      0f, invMass1 + invMass2)
    val K2 = Matrix2f(invI1 * r1.y * r1.y, -invI1 * r1.x * r1.y,
                      -invI1 * r1.x * r1.y, invI1 * r1.x * r1.x)
    val K3 = Matrix2f(invI2 * r2.y * r2.y, -invI2 * r2.x * r2.y,
                      -invI2 * r2.x * r2.y, invI2 * r2.x * r2.x)
    val K = K1 + K2 + K3
    pivotMass = K.invert

    motorMass = 1.0f / (invI1 + invI2)

    if (!enableMotor) {
      motorForce = 0.0f
    }

    if (enableLimit) {
      val jointAng = this.jointAngle
      if ((upperAngle - lowerAngle).abs < 2.0f * Settings.angularSlop) {
        limitState = LimitState.EQUAL_LIMITS
      } else if (jointAng <= lowerAngle) {
        if (limitState != LimitState.AT_LOWER_LIMIT) {
          limitForce = 0.0f
        }
        limitState = LimitState.AT_LOWER_LIMIT
      } else if (jointAng >= upperAngle) {
        if (limitState != LimitState.AT_UPPER_LIMIT) {
          limitForce = 0.0f
        }
        limitState = LimitState.AT_UPPER_LIMIT
      }else {
        limitState = LimitState.INACTIVE_LIMIT
        limitForce = 0.0f
      }
    } else {
      limitForce = 0.0f
    }

    if (step.warmStarting) {
      b1.linearVelocity -= step.dt * invMass1 * pivotForce
      b1.angularVelocity -= step.dt * invI1 * ((r1 × pivotForce) + motorForce + limitForce)

      b2.linearVelocity += step.dt * invMass2 * pivotForce
      b2.angularVelocity += step.dt * invI2 * ((r2 × pivotForce) + motorForce + limitForce)
    } else {
      pivotForce = Vector2f.Zero
      motorForce = 0.0f
      limitForce = 0.0f
    }

    limitPositionImpulse = 0.0f
  }

  def solveVelocityConstraints(step: TimeStep) {
    val b1 = body1
    val b2 = body2

    val r1 = b1.transform.rot * (localAnchor1 - b1.localCenter)
    val r2 = b2.transform.rot * (localAnchor2 - b2.localCenter)

    // Solve point-to-point constraint
    val pivotCdot = b2.linearVelocity + ((b2.angularVelocity × r2) - b1.linearVelocity - (b1.angularVelocity × r1))
    val pivotF = pivotMass * pivotCdot * (-step.invDt)
    pivotForce += pivotF

    val P = pivotF * step.dt
    b1.linearVelocity -= b1.invMass * P
    b1.angularVelocity -= b1.invI * (r1 × P)

    b2.linearVelocity += b2.invMass * P
    b2.angularVelocity += b2.invI * (r2 × P)

    if (enableMotor && limitState != LimitState.EQUAL_LIMITS) {
      val motorCdot = b2.angularVelocity - b1.angularVelocity - motorSpeed
      var motorF = -step.invDt * motorMass * motorCdot
      val oldMotorForce = motorForce
      motorForce = MathUtil.clamp(motorForce + motorF, -maxMotorTorque, maxMotorTorque)
      motorF = motorForce - oldMotorForce

      val P2 = step.dt * motorF
      b1.angularVelocity -= b1.invI * P2
      b2.angularVelocity += b2.invI * P2
    }

    if (enableLimit && limitState != LimitState.INACTIVE_LIMIT) {
      val limitCdot = b2.angularVelocity - b1.angularVelocity
      var limitF = -step.invDt * motorMass * limitCdot

      if (limitState == LimitState.EQUAL_LIMITS) {
        limitForce += limitF
      } else if (limitState == LimitState.AT_LOWER_LIMIT) {
        val oldLimitForce = limitForce
        limitForce = MathUtil.max(limitForce + limitF, 0.0f)
        limitF = limitForce - oldLimitForce
      } else if (limitState == LimitState.AT_UPPER_LIMIT) {
        val oldLimitForce = limitForce
        limitForce = MathUtil.min(limitForce + limitF, 0.0f)
        limitF = limitForce - oldLimitForce
      }

      val P2 = step.dt * limitForce
      b1.angularVelocity -= b1.invI * P2
      b2.angularVelocity += b2.invI * P2
    }
  }

  def solvePositionConstraints() = {
    val b1 = body1
    val b2 = body2

    // Solve point-to-point position error.
    val r1 = b1.transform.rot * (localAnchor1 - b1.localCenter)
    val r2 = b2.transform.rot * (localAnchor2 - b2.localCenter)

    val p1 = b1.sweep.c + r1
    val p2 = b2.sweep.c + r2
    val ptpC = p2 - p1

    val positionError = ptpC.length

    // Prevent overly large corrections.
    //public b2Vec2 dpMax(b2_maxLinearCorrection, b2_maxLinearCorrection);
    //ptpC = b2Clamp(ptpC, -dpMax, dpMax);

    val invMass1 = b1.invMass
    val invMass2 = b2.invMass
    val invI1 = b1.invI
    val invI2 = b2.invI

    val K1 = Matrix2f(invMass1 + invMass2, 0.0f,
                      0.0f, invMass1 + invMass2)
    val K2 = Matrix2f(invI1 * r1.y * r1.y, -invI1 * r1.x * r1.y,
                      -invI1 * r1.x * r1.y, invI1 * r1.x * r1.x)
    val K3 = Matrix2f(invI2 * r2.y * r2.y, -invI2 * r2.x * r2.y,
                      -invI2 * r2.x * r2.y, invI2 * r2.x * r2.x)

    val K = K1 + K2 + K3
    val impulse = K.solve(-ptpC)

    b1.sweep.c -= b1.invMass * impulse
    b1.sweep.a -= b1.invI * (r1 × impulse)

    b2.sweep.c += b2.invMass * impulse
    b2.sweep.a += b2.invI * (r2 × impulse)

    b1.synchronizeTransform()
    b2.synchronizeTransform()

    // Handle limits.
    var angularError = 0.0f


    if (enableLimit && limitState != LimitState.INACTIVE_LIMIT) {
      val angle = jointAngle
      var limitImpulse = 0.0f

      if (limitState == LimitState.EQUAL_LIMITS) {
        // Prevent large angular corrections
        val limitC = MathUtil.clamp(angle, -Settings.maxAngularCorrection, Settings.maxAngularCorrection)
        limitImpulse = -motorMass * limitC
        angularError = limitC.abs
      } else if (limitState == LimitState.AT_LOWER_LIMIT) {
        var limitC = angle - lowerAngle
        angularError = MathUtil.max(0.0f, -limitC)

        // Prevent large angular corrections and allow some slop.
        limitC = MathUtil.clamp(limitC + Settings.angularSlop, -Settings.maxAngularCorrection, 0.0f)
        limitImpulse = -motorMass * limitC
        val oldLimitImpulse = limitPositionImpulse
        limitPositionImpulse = MathUtil.max(limitPositionImpulse + limitImpulse, 0.0f)
        limitImpulse = limitPositionImpulse - oldLimitImpulse
      } else if (limitState == LimitState.AT_UPPER_LIMIT) {
        var limitC = angle - upperAngle
        angularError = MathUtil.max(0.0f, limitC)

        // Prevent large angular corrections and allow some slop.
        limitC = MathUtil.clamp(limitC - Settings.angularSlop, 0.0f, Settings.maxAngularCorrection)
        limitImpulse = -motorMass * limitC
        val oldLimitImpulse = limitPositionImpulse
        limitPositionImpulse = MathUtil.min(limitPositionImpulse + limitImpulse, 0.0f)
        limitImpulse = limitPositionImpulse - oldLimitImpulse
      }

      b1.sweep.a -= b1.invI * limitImpulse
      b2.sweep.a += b2.invI * limitImpulse

      b1.synchronizeTransform()
      b2.synchronizeTransform()
    }

    positionError <= Settings.linearSlop && angularError <= Settings.angularSlop
  }

  def anchor1 = body1.toWorldPoint(localAnchor1)

  def anchor2 = body2.toWorldPoint(localAnchor2)

  def reactionForce = pivotForce

  def reactionTorque = limitForce

  def jointAngle = body2.sweep.a - body1.sweep.a - referenceAngle

  def jointSpeed = body2.angularVelocity - body1.angularVelocity

  def setLimits(lower: Float, upper: Float) {
    assert(lower <= upper)
    lowerAngle = lower
    upperAngle = upper
  }
}
