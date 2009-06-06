package org.villane.box2d.shapes

import vecmath._
import vecmath.Preamble._
import Settings.ε

/**
 * Like a circle shape of zero radius, except that it has a finite mass.
 */
class Point(val pos: Vector2f, val mass: Float) extends Shape {
  // TODO Erkki is this good? Should ask on forums
  val radius = Settings.polygonRadius

  def testPoint(t: Transform2f, p: Vector2f) = false
  def testSegment(t: Transform2f, lambda: Float, normal: Vector2f) {}

  def computeSubmergedArea(normal: Vector2f, offset: Float, t: Transform2f) =
    (0f,Vector2f.Zero)

  def computeAABB(t: Transform2f) = {
    val p = t * pos
    AABB(p - ε, p + ε)
  }

  def computeSweepRadius(pivot: Vector2f) = (pos - pivot).length + Settings.toiSlop

  def computeMass(density: Float) = Mass(mass, pos, 0)

}
