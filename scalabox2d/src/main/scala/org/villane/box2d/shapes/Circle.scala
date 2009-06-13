package org.villane.box2d.shapes

import vecmath._
import vecmath.Preamble._
import Settings.ε

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

  /**
   * Collision Detection in Interactive 3D Environments by Gino van den Bergen
   * From Section 3.1.2
   * x = s + a * r
   * norm(x) = radius
   */
  def testSegment(t: Transform2f, segment: Segment, maxLambda: Float): SegmentCollide = {
    val p = t * pos
    val s = segment.p1 - p
    val b = (s dot s) - radius * radius

    // Does the segment start inside the circle?
    if (b < 0.0f)
      return SegmentCollide.startsInside(0, Vector2f.Zero)

    // Solve quadratic equation.
    val r = segment.p2 - segment.p1
    val c = s dot r
    val rr = r dot r
    val sigma = c * c - rr * b

    // Check for negative discriminant and short segment.
    if (sigma < 0.0f || rr < ε)
      return SegmentCollide.Miss

    // Find the point of intersection of the line with the circle.
    var a = -(c + sqrt(sigma))

    // Is the intersection point on the segment?
    if (0.0f <= a && a <= maxLambda * rr) {
      a /= rr
      return SegmentCollide.hit(a, (s + a * r).normalize)
    }

	SegmentCollide.Miss
  }

  def computeAABB(t: Transform2f) = {
    val p = t * pos
    AABB(p - radius, p + radius)
  }

  def computeMass(density: Float) = {
    val mass = density * π * radius * radius
    // inertia about the local origin
    val i = mass * (0.5f * radius * radius + (pos ∙ pos))
    Mass(mass, pos, i)
  }

  def computeSubmergedArea(normal: Vector2f, offset: Float, t: Transform2f):
    (Float, Vector2f) = {
    val p = t * pos
    val l = -((normal dot p) - offset)
    if (l < -radius + ε) {
      // Completely dry
      return (0, Vector2f.Zero)
    }
    if (l > radius) {
      // Completely wet
      return (π * radius * radius, p)
    }

    //Magic
    val r2 = radius * radius
    val l2 = l * l
    val area = r2 * (asin(l / radius) + π / 2) + l * sqrt(r2 - l2)
    val com = -2.0f / 3.0f * pow(r2-l2, 1.5f) / area

    (area, p + normal * com)
  }

  def computeSweepRadius(pivot: Vector2f) = {
    // ERKKI : in Box2D now it is: = distance(pos, pivot)
    val d = pos - pivot
    d.length + radius - Settings.toiSlop
  }

}