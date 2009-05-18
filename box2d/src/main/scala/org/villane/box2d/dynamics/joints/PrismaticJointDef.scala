package org.villane.box2d.dynamics.joints

import vecmath.Vector2f

class PrismaticJointDef extends JointDef {
  /** The local anchor point relative to body1's origin. */
  var localAnchor1 = Vector2f.Zero

  /** The local anchor point relative to body2's origin. */
  var localAnchor2 = Vector2f.Zero

  /** The local translation axis in body1. */
  var localAxis1 = Vector2f.Zero
  
  /** The constrained angle between the bodies: body2_angle - body1_angle. */
  var referenceAngle = 0f

  /** A flag to enable joint limits. */
  var enableLimit = false

  /** The lower translation limit, usually in meters. */
  var lowerTranslation = 0f

  /** The upper translation limit, usually in meters. */
  var upperTranslation = 0f

  /** A flag to enable the joint motor. */
  var enableMotor = false

  /** The desired motor speed. Usually in radians per second. */
  var motorSpeed = 0f

  /**
   * The maximum motor torque used to achieve the desired motor speed.
   * Usually in N-m.
   */
  var maxMotorForce = 0f

  /**
   * Initialize the bodies, anchors, and reference angle using the world anchor.
   */
  def this(b1: Body, b2: Body, anchor: Vector2f, axis: Vector2f) {
    this()
    body1 = b1;
    body2 = b2;
    localAnchor1 = body1.toLocalPoint(anchor)
    localAnchor2 = body2.toLocalPoint(anchor)
    localAxis1 = body1.toLocalVector(axis) 
    referenceAngle = body2.angle - body1.angle
  }

}
