package org.villane.box2d.dynamics.joints

import vecmath._
import vecmath.Preamble._

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
  var maxForce: Scalar = 0f

  /** The response speed. */
  var frequencyHz: Scalar = 5f

  /** The damping ratio. 0 = no damping, 1 = critical damping. */
  var dampingRatio: Scalar = 0.7f

  /** The time step used in the simulation. */
  var timeStep: Scalar = 1f / 60f
}
