package org.villane.box2d.testbed

import org.villane.vecmath.Vector2

/**
 * Settings for the current test.  Mostly self-explanatory.
 * <BR><BR>
 * The settings from here are applied during AbstractExample::step().
 *
 */
class TestSettings {
  var hz = 60
  var iterationCount = 10
  var gravity = Vector2(0f, -9.81f)

  var enableWarmStarting = true
  var enablePositionCorrection = true
  var enableTOI = true
  var enableSleeping = true

  /** Pause the simulation */
  var pause = false
  /** Take a single timestep */
  var singleStep = false
  /** True if we should reset the demo for the next frame. */
  var reset = false

  var drawShapes = true
  var drawJoints = true
  var drawCoreShapes = false
  var drawCOMs = false
  var drawStats = false
  var drawImpulses = false
  var drawAABBs = false
  var drawPairs = false
  var drawContactPoints = false
  var drawContactNormals = false
  var drawContactForces = false
  var drawFrictionForces = false

  var testIndex = 0
}
