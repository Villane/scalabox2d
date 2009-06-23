package org.villane.box2d.svg

import vecmath.Vector2

/**
 * @author Mason Green
 */
object ConvexHull {
  /**
   * Melkman's Algorithm
   * http://www.ams.sunysb.edu/~jsbm/courses/345/melkman.pdf
   * Returns a convex hull in CCW order
   * 
   * The input has to be a polygon in either CCW or CW order.
   */
  def convexHull(V: Array[Vector2]) = {
    def left(a: Vector2, b: Vector2, c: Vector2) =
      (b.x - a.x) * (c.y - a.y) - (c.x - a.x) * (b.y - a.y) > 0

    val n = V.length
    val D = new Array[Vector2](2 * n + 1)
    var bot = n - 2
    var top = bot + 3

    D(bot) = V(2)
    D(top) = V(2)

    if (left(V(0), V(1), V(2))) {
      D(bot+1) = V(0)
      D(bot+2) = V(1)
    } else {
      D(bot+1) = V(1)
      D(bot+2) = V(0)
    }

    var i = 3
    while(i < n) {
      while (left(D(bot), D(bot+1), V(i)) && left(D(top-1), D(top), V(i))) {
        i += 1
      }
      while (!left(D(top-1), D(top), V(i))) top -= 1
      top += 1; D(top) = V(i)
      while (!left(D(bot), D(bot+1), V(i))) bot += 1
      bot -= 1; D(bot) = V(i)
      i += 1
    }

    val H = new Array[Vector2](top - bot)
    var h = 0
    while(h < (top - bot)) {
      H(h) = D(bot + h)
      h += 1
    }
    H
  }

}
