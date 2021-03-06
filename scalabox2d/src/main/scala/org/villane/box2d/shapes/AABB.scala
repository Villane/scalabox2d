package org.villane.box2d.shapes

import vecmath._
import vecmath.Preamble._

object AABB {
  def centered(center: Vector2, size: Vector2) = AABB(
    center - size / 2, center + size / 2
  )
}

/**
 * An axis-aligned bounding box
 */
case class AABB(lowerBound: Vector2, upperBound: Vector2) {
  def size = upperBound - lowerBound
  def center = lowerBound + (upperBound - lowerBound) / 2

  def combineWith(other: AABB) = AABB(
    min(lowerBound, other.lowerBound),
    max(upperBound, other.upperBound)
  )

  /** Does this aabb contain the provided AABB. */
  def contains(other: AABB) = {
    lowerBound.x <= other.lowerBound.x &&
    lowerBound.y <= other.lowerBound.y &&
    upperBound.x >= other.upperBound.x &&
    upperBound.y >= other.upperBound.y
  }

  /** Check if AABBs overlap. */
  def overlaps(other: AABB) = {
    val d1 = other.lowerBound - upperBound
    val d2 = lowerBound - other.upperBound
    ! (d1.x > 0.0f || d1.y > 0.0f || d2.x > 0.0f || d2.y > 0.0f)
  }

  /** Verify that the bounds are sorted. */
  def isValid = {
    val d = upperBound - lowerBound
    val valid = (d.x >= 0 && d.y >= 0)
    valid && lowerBound.isValid && upperBound.isValid
  }

}
