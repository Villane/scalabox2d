package org.villane.box2d.shapes

import vecmath._
import vecmath.Preamble._

object Mass {
  val Zero = Mass(0f, Vector2.Zero, 0f)
}

/** This holds the mass data computed for a shape. */
case class Mass(
  /** The mass of the shape, usually in kilograms. */
  mass: Scalar,
  /** The position of the shape's centroid relative to the shape's origin. */
  center: Vector2,
  /** The rotational inertia of the shape. */
  I: Scalar
)
