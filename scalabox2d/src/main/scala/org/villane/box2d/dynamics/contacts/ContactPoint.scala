package org.villane.box2d.dynamics.contacts

import vecmath.Vector2
import shapes.Shape
import collision.ContactID

case class ContactPoint(
  /** The first shape */
  fixture1: Fixture,
  /** The second shape */
  fixture2: Fixture,
  /** Position in world coordinates */
  pos: Vector2,
  /** Velocity of point on body2 relative to point on body1 (pre-solver) */
  velocity: Vector2,
  /** Points from shape1 to shape2 */
  normal: Vector2,
  /** The separation is negative when shapes are touching */
  separation: Float,
  /** The combined friction coefficient */
  friction: Float,
  /** The combined restitution coefficient */
  restitution: Float,
  /** The contact id identifies the features in contact */
  id: ContactID
)
