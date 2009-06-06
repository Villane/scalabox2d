package org.villane.box2d.shapes

import vecmath._
import vecmath.Preamble._
import settings.Settings
import MathUtil.π

/**
 * Circle
 * 
 * pos - local position within a body (background system)
 * radius - radius of the circle
 */
class Circle(defn: CircleDef) extends Shape {
  radius = defn.radius
  var pos = defn.pos

  def computeSweepRadius(pivot: Vector2f) = {
    val d = pos - pivot
    d.length + radius - Settings.toiSlop
  }

  def testPoint(t: Transform2f, p: Vector2f) = {
   	val center = t * pos
   	val d = p - center
  	(d ∙ d) <= (radius * radius)
  }

  def testSegment(t: Transform2f, lambda: Float, normal: Vector2f) {}

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