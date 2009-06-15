package org.villane.box2d.shapes

import vecmath._
import vecmath.Preamble._
import Settings.ε

/**
 * A single edge from v1 to v2
 */
class Edge(val v1: Vector2, val v2: Vector2) extends Shape {
  // Erkki: is this correct?
  val radius = Settings.polygonRadius

  val direction = (v2 - v1).normalize
  val length = (v2 - v1).length
  val normal = direction.normal

  val corner1Dir = normal
  val corner2Dir = normal * -1.0f
  val corner1Convex = true // TODO!!!
  val corner2Convex = true // TODO!!!

  def testPoint(t: Transform2, p: Vector2) = false

  def testSegment(t: Transform2, segment: Segment, maxLambda: Float) =
    new Segment(t * v1, t * v2).testSegment(segment, maxLambda)

  def computeAABB(t: Transform2) = {
    val p1 = t * v1
    val p2 = t * v2
    val r = Vector2(radius, radius)
    AABB(min(p1, p2), max(p1, p2))
  }

  // ERKKI Shouldn't the center be v1 + (v2 - v1) / 2 ? Or is it not important?
  def computeMass(density: Float) = Mass(0, v1, 0)

  def computeSubmergedArea(normal: Vector2, offset: Float, t: Transform2):
    (Float, Vector2) = {
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
        return (0, Vector2.Zero)
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

  def computeSweepRadius(pivot: Vector2) = {
    val ds1 = distanceSquared(v1, pivot)
    val ds2 = distanceSquared(v2, pivot)
    sqrt(max(ds1, ds2))
  }

}
