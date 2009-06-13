package org.villane.box2d.dynamics.joints

import vecmath.Vector2

class PointingDeviceJointDef extends JointDef {
  /**
   * The initial world target point. This is assumed
   * to coincide with the body anchor initially.
   */
  var target = Vector2.Zero

  /** 
   * The maximum constraint force that can be exerted
   * to move the candidate body. Usually you will express
   * as some multiple of the weight (multiplier * mass * gravity).
   */
  var maxForce = 0f

  /** The response speed. */
  var frequencyHz = 5f

  /** The damping ratio. 0 = no damping, 1 = critical damping. */
  var dampingRatio = 0.7f

  /** The time step used in the simulation. */
  var timeStep = 1f / 60f
}
