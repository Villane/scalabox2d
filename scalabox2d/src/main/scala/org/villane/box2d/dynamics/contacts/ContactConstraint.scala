package org.villane.box2d.dynamics.contacts

import vecmath.Vector2
import collision.Manifold
import dynamics.Body

class ContactConstraint(
  val points: Array[ContactConstraintPoint],
  val normal: Vector2,
  val manifold: Manifold,
  val body1: Body,
  val body2: Body,
  val friction: Float,
  val restitution: Float
)

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
