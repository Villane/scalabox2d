package org.villane.box2d.dynamics.joints

import vecmath.Vector2

/**
 * Revolute joint definition. This requires defining an
 * anchor point where the bodies are joined. The definition
 * uses local anchor points so that the initial configuration
 * can violate the constraint slightly. You also need to
 * specify the initial relative angle for joint limits. This
 * helps when saving and loading a game.
 * The local anchor points are measured from the body's origin
 * rather than the center of mass because:
 * 1. you might not know where the center of mass will be.
 * 2. if you add/remove shapes from a body and recompute the mass,
 *    the joints will be broken.
 */
class RevoluteJointDef extends JointDef {
  /** The local anchor point relative to body1's origin. */
  var localAnchor1 = Vector2.Zero

  /** The local anchor point relative to body2's origin. */
  var localAnchor2 = Vector2.Zero

  /** The body2 angle minus body1 angle in the reference state (radians). */
  var referenceAngle = 0f

  /** A flag to enable joint limits. */
  var enableLimit = false

  /** The lower angle for the joint limit (radians). */
  var lowerAngle = 0f

  /** The upper angle for the joint limit (radians). */
  var upperAngle = 0f

  /** A flag to enable the joint motor. */
  var enableMotor = false

  /** The desired motor speed. Usually in radians per second. */
  var motorSpeed = 0f

  /**
   * The maximum motor torque used to achieve the desired motor speed.
   * Usually in N-m.
   */
  var maxMotorTorque = 0f

  /**
   * Initialize the bodies, anchors, and reference angle using the world anchor.
   */
  def this(b1: Body, b2: Body, anchor: Vector2) {
    this()
    body1 = b1;
    body2 = b2;
    localAnchor1 = body1.toLocalPoint(anchor)
    localAnchor2 = body2.toLocalPoint(anchor)
    referenceAngle = body2.angle - body1.angle
  }

}
