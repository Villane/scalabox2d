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
class Circle(defn: CircleDef) extends Shape(defn) {
  val pos = defn.pos
  val radius = defn.radius

  def updateSweepRadius(center: Vector2f) {
    // Update the sweep radius (maximum radius) as measured from
    // a local center point.
    val d = pos - center
    sweepRadius = d.length + radius - Settings.toiSlop
  }
    
  def testPoint(t: Transform2f, p: Vector2f) = {
   	val center = t * pos
   	val d = p - center
  	(d ∙ d) <= (radius * radius)
  }
  
  def computeAABB(t: Transform2f) = {
    val p = t * pos
    AABB(p - radius, p + radius)
  }
  
  def computeSweptAABB(t1: Transform2f, t2: Transform2f) = {
    val p1 = t1 * pos
    val p2 = t2 * pos
    val lower = min(p1, p2)
    val upper = max(p1, p2)
    AABB(lower - radius, upper + radius)
    //System.out.println("Circle swept AABB: " + aabb.lowerBound + " " + aabb.upperBound);
    //System.out.println("Transforms: "+transform1.position+ " " + transform2.position+"\n");
  }

  def computeMass() = {
    val mass = density * π * radius * radius;
    // inertia about the local origin
    val i = mass * (0.5f * radius * radius + (pos ∙ pos))
    Mass(mass, pos, i)
  }
}