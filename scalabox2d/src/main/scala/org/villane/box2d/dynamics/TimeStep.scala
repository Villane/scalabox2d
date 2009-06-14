package org.villane.box2d.dynamics

import vecmath.Preamble._

/**
 * A holder for time step information.
 */
class TimeStep(
  // time step
  var dt: Scalar
) {
  // inverse time step (0 if dt == 0).
  var invDt: Scalar = if (dt > 0f) 1 / dt else 0f
  var dtRatio: Scalar = 0f
  var warmStarting = false
  var positionCorrection = false
  var maxIterations = 0
}
