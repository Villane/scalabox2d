package org.villane.box2d.collision

import vecmath._
import vecmath.Preamble._

/**
 * A manifold point is a contact point belonging to a contact
 * manifold. It holds details related to the geometry and dynamics
 * of the contact points.
 * The point is stored in local coordinates because CCD
 * requires sub-stepping in which the separation is stale.
 */
case class ManifoldPoint(
  /** Local position of the contact point in body1 */
  localPoint1: Vector2,
  /** Local position of the contact point in body2 */
  localPoint2: Vector2,
  /** The separation of the shapes along the normal vector */
  separation: Scalar,
  /** Uniquely identifies a contact point between two shapes */
  id: ContactID
) {
  /** The non-penetration force */
  var normalImpulse: Scalar = 0f
  /** The friction force */
  var tangentImpulse: Scalar = 0f
}
