package org.villane.box2d.dynamics.contacts

import vecmath.Vector2f
import shapes.Shape
import collision.ContactID

/** This structure is used to report contact point results. */
case class ContactResult(
  /** The first shape */
  shape1: Shape,
  /** The second shape */
  shape2: Shape,
  /** Position in world coordinates */
  pos: Vector2f,
  /** Points from shape1 to shape2 */
  normal: Vector2f,
  /** The normal impulse applied to body2 */
  normalImpulse: Float,
  /** The tangent impulse applied to body2 */
  tangentImpulse: Float,
  /** The contact id identifies the features in contact */
  id: ContactID
)
