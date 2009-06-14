package org.villane.box2d.dynamics.contacts

import vecmath._
import vecmath.Preamble._
import shapes.Shape
import collision.ContactID

/** This structure is used to report contact point results. */
case class ContactResult(
  /** The first shape */
  fixture1: Fixture,
  /** The second shape */
  fixture2: Fixture,
  /** Position in world coordinates */
  pos: Vector2,
  /** Points from shape1 to shape2 */
  normal: Vector2,
  /** The normal impulse applied to body2 */
  normalImpulse: Scalar,
  /** The tangent impulse applied to body2 */
  tangentImpulse: Scalar,
  /** The contact id identifies the features in contact */
  id: ContactID
)
