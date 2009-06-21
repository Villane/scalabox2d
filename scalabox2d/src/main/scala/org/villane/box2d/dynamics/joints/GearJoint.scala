package org.villane.box2d.dynamics.joints

import vecmath._
import vecmath.Preamble._

/**
 * A gear joint is used to connect two joints together. Either joint
 * can be a revolute or prismatic joint. You specify a gear ratio
 * to bind the motions together:
 * coordinate1 + ratio * coordinate2 = constant
 * The ratio can be negative or positive. If one joint is a revolute joint
 * and the other joint is a prismatic joint, then the ratio will have units
 * of length or units of 1/length.
 * <BR><em>Warning</em>: The revolute and prismatic joints must be attached to
 * fixed bodies (which must be body1 on those joints).
 */
class GearJoint(defn: GearJointDef) extends Joint(defn) {
  // Gear Joint:
  // C0 = (coordinate1 + ratio * coordinate2)_initial
  // C = C0 - (cordinate1 + ratio * coordinate2) = 0
  // Cdot = -(Cdot1 + ratio * Cdot2)
  // J = -[J1 ratio * J2]
  // K = J * invM * JT
  // = J1 * invM1 * J1T + ratio * ratio * J2 * invM2 * J2T
  //
  // Revolute:
  // coordinate = rotation
  // Cdot = angularVelocity
  // J = [0 0 1]
  // K = J * invM * JT = invI
  //
  // Prismatic:
  // coordinate = dot(p - pg, ug)
  // Cdot = dot(v + cross(w, r), ug)
  // J = [ug cross(r, ug)]
  // K = J * invM * JT = invMass + invI * cross(r, ug)^2

  val joint1 = defn.joint1
  val joint2 = defn.joint2
  assert(joint1.isInstanceOf[RevoluteJoint] || joint2.isInstanceOf[PrismaticJoint])
  assert(joint2.isInstanceOf[RevoluteJoint] || joint2.isInstanceOf[PrismaticJoint])
  assert(joint1.body1.isStatic)
  assert(joint2.body1.isStatic)

  val ground1 = joint1.body1
  val ground2 = joint2.body1
  val groundAnchor1 = joint1.localAnchor1
  val localAnchor1 = joint1.localAnchor2
  val groundAnchor2 = joint2.localAnchor1
  val localAnchor2 = joint2.localAnchor2

  def getCoordinate(joint: Joint) = joint match {
    case pj: PrismaticJoint => pj.jointTranslation
    case rj: RevoluteJoint => rj.jointAngle
  }
  val coordinate1 = getCoordinate(joint1)
  val coordinate2 = getCoordinate(joint2)

  val ratio = defn.ratio
  val constant = coordinate1 + ratio * coordinate2

  var J = Jacobian.Zero

  /** Effective mass */
  var mass = 0f

  /** Force for accumulation/warm starting. */
  var force = 0f

  def anchor1 = body1.toWorldPoint(localAnchor1)
  def anchor2 = body2.toWorldPoint(localAnchor2)
  // TODO_ERIN not tested
  def reactionForce = Vector2(force * J.linear2.x, force * J.linear2.y)
  def reactionTorque = {
    // TODO_ERIN not tested
    val r = body2.transform.rot * (localAnchor2 - body2.localCenter)
    val F = reactionForce
    force * J.angular2 - (r cross F)
  }

  def initVelocityConstraints(step: TimeStep) = {
    val g1 = ground1
    val g2 = ground2
    val b1 = body1
    val b2 = body2

    var K = 0.0f
    var ug = Vector2.Zero
    var r = Vector2.Zero
    var ang1 = 0f
    var lin1 = Vector2.Zero
    var ang2 = 0f
    var lin2 = Vector2.Zero

    joint1 match {
      case rj: RevoluteJoint =>
        ang1 = -1
        K += b1.invI
      case pj: PrismaticJoint =>
        ug = g1.transform.rot * pj.localXAxis1
        r = b1.transform.rot * (localAnchor1 - b1.localCenter)
        val crug = r cross ug
        lin1 = -ug
        ang1 = -crug
        K += b1.invMass + b1.invI * crug * crug
    }

    joint2 match {
      case rj: RevoluteJoint =>
        ang2 = -ratio
        K += ratio * ratio * b2.invI
      case pj: PrismaticJoint =>
        ug = g2.transform.rot * pj.localXAxis1
        r = b2.transform.rot * (localAnchor2 - b2.localCenter)
        val crug = r cross ug
        ug *= -ratio
        lin2 = ug
        ang2 = -ratio * crug
        K += ratio * ratio * (b2.invMass + b2.invI * crug * crug)
    }

    J = Jacobian(lin1, ang1, lin2, ang2)

    // Compute effective mass.
    assert (K > 0.0f)
    mass = 1.0f / K

    if (step.warmStarting) {
      // Warm starting.
      updateVelocities(step.dt * force)
    } else {
      force = 0.0f
    }
  }

  private def updateVelocities(P: Float) {
    body1.linearVelocity += body1.invMass * P * J.linear1
    body1.angularVelocity += body1.invI * P * J.angular1
    body2.linearVelocity += body2.invMass * P * J.linear2
    body2.angularVelocity += body2.invI * P * J.angular2
  }

  def solveVelocityConstraints(step: TimeStep) = {
    val b1 = body1
    val b2 = body2

   	val Cdot = J.compute(b1.linearVelocity, b1.angularVelocity,
                         b2.linearVelocity, b2.angularVelocity)

    var forceL = -step.invDt * mass * Cdot
    force += forceL
    updateVelocities(step.dt * forceL)
  }

  def solvePositionConstraints: Boolean = {
    var linearError = 0.0f

    val b1 = body1
    val b2 = body2

    val coord1 = getCoordinate(joint1)
    val coord2 = getCoordinate(joint2)

    val C = constant - (coord1 + ratio * coord2)
    val impulse = -mass * C

    b1.sweep.c += b1.invMass * impulse * J.linear1
    b1.sweep.a += b1.invI * impulse * J.angular1
    b2.sweep.c += b2.invMass * impulse * J.linear2
    b2.sweep.a += b2.invI * impulse * J.angular2

    b1.synchronizeTransform
    b2.synchronizeTransform

    linearError < Settings.linearSlop
  }

}
