package org.villane.box2d.dynamics.controllers

import vecmath._
import vecmath.Preamble._

/** Applies simplified gravity between every pair of bodies */
abstract class GravityController extends Controller {
  /** Specifies the strength of the gravitiation force */
  var G = 0f

  /** If true, gravity is proportional to r^-2, otherwise r^-1 */
  var invSqr = false

  def step(step: TimeStep) =
    for (b1 <- bodies; b2 <- bodies) if (b1 != b2) {
      val d = b2.worldCenter - b1.worldCenter
      val r2 = d.lengthSquared
      if (r2 >= Settings.Epsilon) {
        val f = if (invSqr)
          G / r2 / sqrt(r2) * b1.mass * b2.mass * d
        else
          G / r2 * b1.mass * b2.mass * d
        b1.applyForce(f, b1.worldCenter)
        b2.applyForce(-1.0f * f, b2.worldCenter)
      }
    }

}
