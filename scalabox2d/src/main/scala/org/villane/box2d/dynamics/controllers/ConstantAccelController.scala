package org.villane.box2d.dynamics.controllers

import vecmath._
import vecmath.Preamble._

/** Applies acceleration every frame */
abstract class ConstantAccelController extends Controller {
  /* The acceleration to apply */
  var A = Vector2.Zero

  def step(step: TimeStep) =
    forAwakeBodies(_.linearVelocity += step.dt * A)

}
