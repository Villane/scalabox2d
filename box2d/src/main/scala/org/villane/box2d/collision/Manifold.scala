package org.villane.box2d.collision

import vecmath.Vector2f

/** A manifold for two touching convex shapes. */
case class Manifold(
  /** The points of contact. */
  points: Seq[ManifoldPoint],
  /**
   * The shared unit normal vector.
   * This is mutable because it may need to be reversed in the last phase of creating a contact.
   * But this definitely should not be changed post contact creation.
   * TODO might want to think of something to make this immutable.
   */
  var normal: Vector2f
) {
  assert(points.length > 0, "Can't create a manifold without points!")
}
