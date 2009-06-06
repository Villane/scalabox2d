package org.villane.box2d.collision

import vecmath._
import shapes.Circle
import Settings.ε

/**
 * Circle/circle overlap solver - for internal use only.
 */
object CircleCollider {
  def collideCircles(circle1: Circle, xf1: Transform2f,
            circle2: Circle, xf2: Transform2f): Option[Manifold] = {
    var p1 = xf1 * circle1.pos
    var p2 = xf2 * circle2.pos
    val d = p2 - p1

    val distSqr = d ∙ d

    val r1 = circle1.radius
    val r2 = circle2.radius
    val radiusSum = r1 + r2
     
    if (distSqr > radiusSum * radiusSum) {
      return None
    }

    var separation = 0f
    var normal: Vector2f = null
    if (distSqr < ε) {
      separation = -radiusSum
      normal = Vector2f.YUnit
    } else {
      val dist = MathUtil.sqrt(distSqr)
      separation = dist - radiusSum
      val a = 1 / dist
      normal = d * a
    }

    p1 += (normal * r1)
    p2 -= (normal * r2)
    val p = (p1 + p2) * 0.5f

    val points = new Array[ManifoldPoint](1)
    val id = ContactID.Zero //use this instead of zeroing through key
    points(0) = ManifoldPoint(xf1 ** p, xf2 ** p, separation, id)
    Some(Manifold(points, normal))
  }
}
