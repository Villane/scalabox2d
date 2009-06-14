package org.villane.box2d.collision

import vecmath._
import vecmath.Preamble._
import shapes._
import collision._
import Settings.Epsilon

object EdgeCircleCollider {
  def collideEdgeAndCircle(edge: Edge, xf1: Transform2,
                           circle: Circle, xf2: Transform2): Option[Manifold] = {
    val c = xf2 * circle.pos
    val cLocal = xf1 ** c

    val n = edge.normal
    val v1 = edge.v1
    val v2 = edge.v2
    val radius = circle.radius

    var separation = 0f

    var d = cLocal - v1

    var dirDist = d dot edge.direction
    if (dirDist <= 0) {
      if ((d dot edge.corner1Dir) < 0) return None
      d = c - xf1 * v1
    } else if (dirDist >= edge.length) {
      d = c - v2
      if ((d dot edge.corner2Dir) > 0) return None
      d = c - xf1 * v2
    } else {
      separation = d dot n
      if (separation > radius || separation < -radius) return None
      separation -= radius

      val id = ContactID.Zero
      val normal = xf1.rot * n
      val pos = c - normal * radius 
      val points = new Array[ManifoldPoint](1)
      points(0) = ManifoldPoint(xf1 ** pos, xf2 ** pos, separation, id)
      return Some(Manifold(points, normal))
    }

    val distSqr = d dot d
    if (distSqr > radius * radius) return None

    val normal = if (distSqr < Epsilon) {
      separation = -radius
      xf1.rot * n
    } else {
      val len = d.length
      separation = len - radius
      d / len
    }

    val id = ContactID.Zero
    val pos = c - normal * radius
    val points = new Array[ManifoldPoint](1)
    points(0) = ManifoldPoint(xf1 ** pos, xf2 ** pos, separation, id)
    Some(Manifold(points, normal))
  }

}
