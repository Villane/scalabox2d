package org.villane.box2d.dynamics

/**
 * A holder for time step information.
 */
class TimeStep(
  // time step
  var dt: Float
) {
  // inverse time step (0 if dt == 0).
  var invDt = if (dt > 0f) 1 / dt else 0f
  var dtRatio = 0f
  var warmStarting = false
  var positionCorrection = false
  var maxIterations = 0
}
