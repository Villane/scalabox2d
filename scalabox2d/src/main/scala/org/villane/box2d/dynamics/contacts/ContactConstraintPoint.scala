package org.villane.box2d.dynamics.contacts

import vecmath.Vector2

class ContactConstraintPoint {
  var localAnchor1 = Vector2.Zero
  var localAnchor2 = Vector2.Zero
  var r1 = Vector2.Zero
  var r2 = Vector2.Zero
  var normalImpulse = 0f
  var tangentImpulse = 0f
  var positionImpulse = 0f
  var normalMass = 0f
  var tangentMass = 0f
  var equalizedMass = 0f
  var separation = 0f
  var velocityBias = 0f
}
