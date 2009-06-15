package org.villane.box2d.shapes

import vecmath._

/** An oriented bounding box. */
case class OBB(
  /** The rotation matrix. */
  rot: Matrix22,
  /** The local centroid. */
  center: Vector2,
  /** The half-widths. */
  extents: Vector2
)