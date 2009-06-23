package org.villane.box2d.dynamics.controllers

import vecmath._
import vecmath.Preamble._

/**
 * Applies top down linear damping to the controlled bodies
 * The damping is calculated by multiplying velocity by a matrix in local co-ordinates.
 * Some examples:
 * 
 * |-a  0|  Standard isotropic damping with strength a
 * | 0 -a|
 * 
 * | 0  a|  Electron in fixed field - a force at right angles to velocity with proportional magnitude
 * |-a  0|
 * 
 * |-a  0|  Differing x and y damping. Useful e.g. for top-down wheels
 * | 0 -b|
 * 
 */
abstract class TensorDampingController extends Controller {
  /** Tensor to use in damping model. Tensor means Matrix here. */
  var T = Matrix22.Zero

  /**
   * Set this to a positive number to clamp the maximum amount of damping done.
   * 
   * Typically one wants maxTimestep to be 1/(max eigenvalue of T),
   * so that damping will never cause something to reverse direction
   */
  var maxTimestep = 0f

  def step(step: TimeStep): Unit = {
    var timestep = step.dt
    if (timestep <= Settings.Epsilon)
      return
    if (timestep > maxTimestep && maxTimestep > 0)
      timestep = maxTimestep

    forAwakeBodies { body =>
      val damping = body.toWorldVector(T * body.toLocalVector(body.linearVelocity))
      body.linearVelocity += timestep * damping
	}
  }

}
