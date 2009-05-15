package org.villane.box2d.shapes

import vecmath._

/**
 * An axis-aligned bounding box
 */
case class AABB(lowerBound: Vector2f, upperBound: Vector2f) {
  def size = upperBound - lowerBound
  def center = lowerBound + (upperBound - lowerBound) / 2

  /** Verify that the bounds are sorted. */
  def isValid = {
    val d = upperBound - lowerBound
    val valid = (d.x >= 0 && d.y >= 0)
    valid && lowerBound.isValid && upperBound.isValid
  }

  /** Check if AABBs overlap. */
  def overlaps(box: AABB) = {
    val d1 = box.lowerBound - upperBound
    val d2 = lowerBound - box.upperBound
    ! (d1.x > 0.0f || d1.y > 0.0f || d2.x > 0.0f || d2.y > 0.0f)
  }
}
