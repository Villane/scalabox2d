package org.villane.box2d.shapes

import vecmath._
import vecmath.Preamble._
import settings.Settings
import settings.Settings.ε

/**
 * Like a circle shape of zero radius, except that it has a finite mass.
 */
class Point(defn: PointDef) extends Shape(defn) {
  val pos = defn.pos
  val mass = defn.mass

  def testPoint(t: Transform2f, p: Vector2f) = false

  def computeAABB(t: Transform2f) = {
    val p = t * pos
    AABB(p - ε, p + ε)
  }

  def computeSweptAABB(t1: Transform2f, t2: Transform2f) = {
    val p1 = t1 * pos
    val p2 = t2 * pos
    val lower = min(p1, p2)
    val upper = max(p1, p2)
    AABB(lower - ε, upper + ε)
  }

  def updateSweepRadius(center: Vector2f) {
    sweepRadius = (pos - center).length + Settings.toiSlop
  }

  def computeMass() = Mass(mass, pos, 0)

}
