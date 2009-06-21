package org.villane.box2d.dynamics.joints

/**
 * Gear joint definition. This definition requires two existing
 * revolute or prismatic joints (any combination will work).
 * The provided joints must attach a dynamic body to a static body.
 * 
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
class GearJointDef extends JointDef {
  /** The first revolute/prismatic joint attached to the gear joint. */
  private[this] var _joint1: Joint = null
  def joint1 = _joint1
  def joint1_=(joint: Joint) = {
    _joint1 = joint
    body1 = joint.body2
  }

  /** The second revolute/prismatic joint attached to the gear joint. */
  private[this] var _joint2: Joint = null
  def joint2 = _joint2
  def joint2_=(joint: Joint) = {
    _joint2 = joint
    body2 = joint.body2
  }

  /** The gear ratio. @see GearJoint for explanation. */
  var ratio = 1f
}
