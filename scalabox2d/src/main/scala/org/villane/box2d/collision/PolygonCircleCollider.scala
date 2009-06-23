package org.villane.box2d.collision

import vecmath._
import shapes._
import collision._
import Settings.ε

object PolygonCircleCollider extends Collider[Polygon, Circle] {

  def collide(polygon: Polygon, xf1: Transform2,
              circle: Circle, xf2: Transform2): Option[Manifold] = {
    // Compute circle position in the frame of the polygon.
    val c = xf2 * circle.pos
    val cLocal = xf1 ** c

    // Find edge with maximum separation.
    var normalIndex = 0
    var separation = -Float.MaxValue

    val vertexCount = polygon.vertexCount
    val vertices = polygon.vertices
    val normals = polygon.normals
    for (i <- 0 until vertexCount) {
      val s = normals(i) ∙ (cLocal - vertices(i))
      if (s > circle.radius) {
        // Early out.
        return None
      }

      if (s > separation) {
        normalIndex = i;
        separation = s;
      }
    }

    // If the center is inside the polygon ...
    if (separation < ε) {
      val id = ContactID(0, normalIndex, ContactID.NullFeature, false)
      val normal = xf1.rot * normals(normalIndex)
      val p = c - (normal * circle.radius)
      val points = new Array[ManifoldPoint](1)
      points(0) = ManifoldPoint(xf1 ** p, xf2 ** p, separation - circle.radius, id)
      return Some(Manifold(points, normal))
    }

    // Project the circle center onto the edge segment.
    val vertIndex1 = normalIndex;
    val vertIndex2 = if (vertIndex1 + 1 < vertexCount) vertIndex1 + 1 else 0
    var e = vertices(vertIndex2) - vertices(vertIndex1)
    val length = e.length
    e /= length // normalize
    assert(length > ε)

    // Project the center onto the edge.
    val u = (cLocal - vertices(vertIndex1)) ∙ e

    val (p, incidentEdge, incidentVertex) = u match {
      case _ if u <= 0f => (vertices(vertIndex1), ContactID.NullFeature, vertIndex1)
      case _ if u >= length => (vertices(vertIndex2), ContactID.NullFeature, vertIndex2)
      case _ => (vertices(vertIndex1) + (e * u), normalIndex, 0)
    }

    var d = cLocal - p
    val dist = d.length
    d /= dist // normalize

    if (dist > circle.radius) {
      return None
    }

    val id = ContactID(0, incidentEdge, incidentVertex, false)
    val normal = xf1.rot * d
    val pos = c - (normal * circle.radius)
    val points = new Array[ManifoldPoint](1)
    points(0) = ManifoldPoint(xf1 ** pos, xf2 ** pos, dist - circle.radius, id)
    Some(Manifold(points, normal))
  }

}
