package org.villane.box2d.dynamics.contacts

import vecmath._
import vecmath.Preamble._

class ContactConstraintPoint {
  var localAnchor1 = Vector2.Zero
  var localAnchor2 = Vector2.Zero
  var r1 = Vector2.Zero
  var r2 = Vector2.Zero
  var normalImpulse: Scalar = 0f
  var tangentImpulse: Scalar = 0f
  var positionImpulse: Scalar = 0f
  var normalMass: Scalar = 0f
  var tangentMass: Scalar = 0f
  var equalizedMass: Scalar = 0f
  var separation: Scalar = 0f
  var velocityBias: Scalar = 0f
}
