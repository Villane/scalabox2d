package org.villane.box2d.dynamics.joints

import vecmath._
import dynamics._

object Joint {
  def create(defn: JointDef): Joint = defn match {
    case d: DistanceJointDef => new DistanceJoint(d)
    case d: RevoluteJointDef => new RevoluteJoint(d)
    case d: PrismaticJointDef => new PrismaticJoint(d)
    case d: PointingDeviceJointDef => new PointingDeviceJoint(d)
  }
}

abstract class Joint(defn: JointDef) {
  val body1 = defn.body1
  val body2 = defn.body2
  val collideConnected = defn.collideConnected
  val userData = defn.userData

  /** Node for connecting bodies. */
  val node1 = if (body2 != null) JointEdge(body2, this) else null

  /** Node for connecting bodies. */
  val node2 = if (body1 != null) JointEdge(body1, this) else null

  var islandFlag = false
  var invDt = 0f

  init
  def init {
    if (body1 != null) body1.jointList = node1 :: body1.jointList
    if (body2 != null) body2.jointList = node2 :: body2.jointList
  }

  /** Get the anchor point on body1 in world coordinates. */
  def anchor1: Vector2
  /** Get the anchor point on body2 in world coordinates. */
  def anchor2: Vector2
  /** Get the reaction force on body2 at the joint anchor. */
  def reactionForce: Vector2
  /** Get the reaction torque on body2. */
  def reactionTorque: Float
  
  def initVelocityConstraints(step: TimeStep)
  def solveVelocityConstraints(step: TimeStep)

  def initPositionConstraints() {}
  /** This returns true if the position errors are within tolerance. */
  def solvePositionConstraints(): Boolean
}
