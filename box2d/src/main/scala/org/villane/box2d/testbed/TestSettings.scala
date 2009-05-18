package org.villane.box2d.testbed

/**
 * Settings for the current test.  Mostly self-explanatory.
 * <BR><BR>
 * The settings from here are applied during AbstractExample::step().
 *
 */
class TestSettings {
  var hz = 60;
  var iterationCount = 10;

  var enableWarmStarting = true;
  var enablePositionCorrection = true;
  var enableTOI = true;

  var pause = false;
  var singleStep = false;

  var drawShapes = true;
  var drawJoints = true;
  var drawCoreShapes = false;
  var drawOBBs = false;
  var drawCOMs = false;
  var drawStats = true;
  var drawImpulses = false;
  var drawAABBs = false;
  var drawPairs = false;
  var drawContactPoints = false;
  var drawContactNormals = false;
  var drawContactForces = false;
  var drawFrictionForces = false;
}
