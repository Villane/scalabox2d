package org.villane.box2d.shapes

import vecmath._
import vecmath.Preamble._

object SegmentCollide {
  val Miss = SegmentCollide(SegmentCollideResult.Miss, 0, Vector2.Zero)
  def hit(lambda: Scalar, normal: Vector2) =
    SegmentCollide(SegmentCollideResult.Hit, lambda, normal)
  def startsInside(lambda: Scalar, normal: Vector2) =
    SegmentCollide(SegmentCollideResult.StartsInside, lambda, normal)
}

case class SegmentCollide(
  result: SegmentCollideResult,
  lambda: Scalar,
  normal: Vector2
)

sealed trait SegmentCollideResult

object SegmentCollideResult {
  object StartsInside extends SegmentCollideResult
  object Miss extends SegmentCollideResult
  object Hit extends SegmentCollideResult
}

/** A line segment */
class Segment(val p1: Vector2, val p2: Vector2) {
  /** Ray cast against this segment with another segment. */
  def testSegment(segment: Segment, maxLambda: Scalar): SegmentCollide = {
    val s = segment.p1
    val r = segment.p2 - s
    val d = p2 - p1
    val n = d cross 1f

    val slop = 100f * Settings.Epsilon
    val denom = -(r dot n)

    // Cull back facing collision and ignore parallel segments.
    if (denom > slop) {
      // Does the segment intersect the infinite line associated with this segment?
      val b = s - p1
      val a = b dot n

      if (0f <= a && a <= maxLambda * denom) {
        val mu2 = -r.x * b.y + r.y * b.x

        // Does the segment intersect this segment?
        if (-slop * denom <= mu2 && mu2 <= denom * (1f + slop))
          return SegmentCollide.hit(a / denom, n.normalize)
      }
    }
    SegmentCollide.Miss
  }
}
