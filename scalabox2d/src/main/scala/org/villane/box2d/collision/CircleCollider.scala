package org.villane.box2d.collision

import vecmath._
import vecmath.Preamble._
import shapes.Circle

/**
 * Circle/circle overlap solver - for internal use only.
 */
object CircleCollider extends Collider[Circle, Circle] {

  def collide(circle1: Circle, xf1: Transform2,
              circle2: Circle, xf2: Transform2): Option[Manifold] = {
    var p1 = xf1 * circle1.pos
    var p2 = xf2 * circle2.pos
    val d = p2 - p1

    val distSqr = d dot d

    val r1 = circle1.radius
    val r2 = circle2.radius
    val radiusSum = r1 + r2

    if (distSqr > radiusSum * radiusSum) return None

    var separation = 0f
    var normal: Vector2 = null
    if (distSqr < Settings.Epsilon) {
      separation = -radiusSum
      normal = Vector2.YUnit
    } else {
      val dist = sqrt(distSqr)
      separation = dist - radiusSum
      val a = 1 / dist
      normal = d * a
    }

    p1 += (normal * r1)
    p2 -= (normal * r2)
    val p = (p1 + p2) * 0.5f

    val points = new Array[ManifoldPoint](1)
    val id = ContactID.Zero
    points(0) = ManifoldPoint(xf1 ** p, xf2 ** p, separation, id)
    Some(Manifold(points, normal))
  }

}
