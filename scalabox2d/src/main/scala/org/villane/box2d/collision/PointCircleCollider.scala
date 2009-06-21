package org.villane.box2d.collision

import vecmath._
import vecmath.Preamble._
import shapes._

object PointCircleCollider extends Collider[Point, Circle] {

  def collide(point: Point, xf1: Transform2,
              circle: Circle, xf2: Transform2): Option[Manifold] = {
    // TODO reuse Circle.testPoint?
    var p1 = xf1 * point.pos
    var p2 = xf2 * circle.pos
    val d = p2 - p1
    val distSqr = d dot d

    val r = circle.radius 

    if (distSqr > r * r) return None

    var separation = 0f
    var normal: Vector2 = null
    if (distSqr < Settings.Epsilon) {
      separation = -r
      normal = Vector2.YUnit
    } else {
      val dist = sqrt(distSqr)
      separation = dist - r
      val a = 1 / dist
      normal = d * a
    }
    val points = new Array[ManifoldPoint](1)
    val id = ContactID.Zero
    val p = p2 - normal * r
    points(0) = ManifoldPoint(xf1 ** p, xf2 ** p, separation, id)
    Some(Manifold(points, normal))
  }

}
