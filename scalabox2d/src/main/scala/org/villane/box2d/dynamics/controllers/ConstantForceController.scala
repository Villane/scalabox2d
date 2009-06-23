package org.villane.box2d.dynamics.controllers

import vecmath._
import vecmath.Preamble._

/** Applies a force every frame */
abstract class ConstantForceController extends Controller {
  /* The force to apply */
  var F = Vector2.Zero

  def step(step: TimeStep) =
    forAwakeBodies(b => b.applyForce(F, b.worldCenter))

}
