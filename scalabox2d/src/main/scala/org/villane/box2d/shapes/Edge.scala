package org.villane.box2d.shapes

import vecmath._
import vecmath.Preamble._
import settings.Settings
import settings.Settings.ε

/**
 * Temporarily Abstract because half-implemented
 */
abstract class Edge(defn: EdgeDef) extends Shape(defn) with SupportsGenericDistance  {
  val v1: Vector2f = defn.vertices(0)
  val v2: Vector2f = defn.vertices(1)

  def testPoint(t: Transform2f, p: Vector2f) = false

  def computeAABB(t: Transform2f) = {
    val p1 = t * v1
    val p2 = t * v2
    AABB(min(p1, p2), max(p1, p2))
  }

  def computeSweptAABB(t1: Transform2f, t2: Transform2f) = {
    val p1 = t1 * v1
    val p2 = t1 * v2
    val p3 = t2 * v1
    val p4 = t2 * v2
    AABB(min(p1, p2, p3, p4), max(p1, p2, p3, p4))
  }

  // ERKKI Shouldn't the center be v1 + (v2 - v1) / 2 ? Or is it not important?
  def computeMass() = Mass(0, v1, 0)

}
