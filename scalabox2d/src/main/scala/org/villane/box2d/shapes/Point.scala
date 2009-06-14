package org.villane.box2d.shapes

import vecmath._
import vecmath.Preamble._
import Settings.ε

/**
 * Like a circle shape of zero radius, except that it has a finite mass.
 */
class Point(val pos: Vector2, val mass: Scalar) extends Shape {
  // TODO Erkki is this good? Should ask on forums
  val radius = Settings.polygonRadius

  def testPoint(t: Transform2, p: Vector2) = false
  def testSegment(t: Transform2, segment: Segment, maxLambda: Scalar) =
    SegmentCollide.Miss

  def computeSubmergedArea(normal: Vector2, offset: Scalar, t: Transform2) =
    (0f,Vector2.Zero)

  def computeAABB(t: Transform2) = {
    val p = t * pos
    AABB(p - ε, p + ε)
  }

  def computeSweepRadius(pivot: Vector2) = (pos - pivot).length + Settings.toiSlop

  def computeMass(density: Scalar) = Mass(mass, pos, 0)

}
