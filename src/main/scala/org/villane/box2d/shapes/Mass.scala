package org.villane.box2d.shapes

import vecmath.Vector2f

object Mass {
  val Zero = Mass(0f, Vector2f.Zero, 0f)
}

/** This holds the mass data computed for a shape. */
case class Mass(
  /** The mass of the shape, usually in kilograms. */
  mass: Float,
  /** The position of the shape's centroid relative to the shape's origin. */
  center: Vector2f,
  /** The rotational inertia of the shape. */
  I: Float
)
