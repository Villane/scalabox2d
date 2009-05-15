package org.villane.box2d.shapes

import vecmath._

/** An oriented bounding box. */
case class OBB(
  /** The rotation matrix. */
  rot: Matrix2f,
  /** The local centroid. */
  center: Vector2f,
  /** The half-widths. */
  extents: Vector2f
)