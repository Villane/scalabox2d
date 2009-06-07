package org.villane.box2d.shapes

import vecmath._
import vecmath.Preamble._
import MathUtil.π

/**
 * Circle
 * 
 * pos - position
 * radius - radius of the circle
 */
class Circle(val pos: Vector2f, val radius: Float) extends Shape {

  def testPoint(t: Transform2f, p: Vector2f) = {
   	val center = t * pos
   	val d = p - center
  	(d ∙ d) <= (radius * radius)
  }

  def testSegment(t: Transform2f, segment: Segment, maxLambda: Float) =
    SegmentCollide.Miss

  def computeSweepRadius(pivot: Vector2f) = {
    val d = pos - pivot
    d.length + radius - Settings.toiSlop
  }

  def computeSubmergedArea(normal: Vector2f, offset: Float, t: Transform2f) =
    (0f,Vector2f.Zero)

  def computeAABB(t: Transform2f) = {
    val p = t * pos
    AABB(p - radius, p + radius)
  }

  def computeMass(density: Float) = {
    val mass = density * π * radius * radius;
    // inertia about the local origin
    val i = mass * (0.5f * radius * radius + (pos ∙ pos))
    Mass(mass, pos, i)
  }

}