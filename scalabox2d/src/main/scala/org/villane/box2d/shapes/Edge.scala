package org.villane.box2d.shapes

import vecmath._
import vecmath.Preamble._
import Settings.ε

/**
 * Temporarily Abstract because half-implemented
 */
class Edge(defn: EdgeChainDef) extends Shape {
  // Erkki: is this correct?
  val radius = Settings.polygonRadius
  val v1: Vector2f = defn.vertices(0)
  val v2: Vector2f = defn.vertices(1)

  def testPoint(t: Transform2f, p: Vector2f) = false

  def testSegment(t: Transform2f, segment: Segment, maxLambda: Float) =
    new Segment(t * v1, t * v2).testSegment(segment, maxLambda)

  def computeAABB(t: Transform2f) = {
    val p1 = t * v1
    val p2 = t * v2
    val r = Vector2f(radius, radius)
    AABB(min(p1, p2), max(p1, p2))
  }

  // ERKKI Shouldn't the center be v1 + (v2 - v1) / 2 ? Or is it not important?
  def computeMass(density: Float) = Mass(0, v1, 0)

  def computeSubmergedArea(normal: Vector2f, offset: Float, t: Transform2f):
    (Float, Vector2f) = {
    //Note that v0 is independent of any details of the specific edge
    //We are relying on v0 being consistent between multiple edges of the same body
    val v0 = offset * normal
    //b2Vec2 v0 = xf.position + (offset - b2Dot(normal, xf.position)) * normal;

    var lv1 = t * v1
    var lv2 = t * v2

    val d1 = (normal dot lv1) - offset
    val d2 = (normal dot lv2) - offset

    if (d1 > 0.0f) {
      if (d2 > 0.0f) {
        return (0, Vector2f.Zero)
      } else {
        lv1 = -d2 / (d1 - d2) * lv1 + d1 / (d1 - d2) * lv2
      }
    } else {
      if (d2 > 0.0f) {
        lv2 = -d2 / (d1 - d2) * lv1 + d1 / (d1 - d2) * lv2
      } else {
        // Nothing
      }
	}

    // v0,v1,v2 represents a fully submerged triangle
    val tri = Polygon.Triangle(v0, v1, v2)
    // ERKKI in Box2D this centroid is not area weighted
    // even as the comment said so
    import Polygon.Triangle.inv3
    (tri.area, inv3 * (v0 + v1 + v2))
  }

  def computeSweepRadius(pivot: Vector2f) = {
    val ds1 = MathUtil.distanceSquared(v1, pivot)
    val ds2 = MathUtil.distanceSquared(v2, pivot)
    MathUtil.sqrt(MathUtil.max(ds1, ds2))
  }

}
