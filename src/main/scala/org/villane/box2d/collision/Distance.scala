package org.villane.box2d.collision

import vecmath._
import vecmath.Preamble._
import settings.Settings
import settings.Settings.ε
import shapes._

object Point {
  def apply(v: Vector2f): Point = Point(v.x, v.y)
}

// This is used for polygon-vs-circle distance.
case class Point(override val x: Float, override val y: Float) extends Vector2f(x,y) with SupportsGenericDistance {
  def support(xf: Transform2f, v: Vector2f) = this
  def getFirstVertex(xf: Transform2f) = this
}

/** Implements the GJK algorithm for computing distance between shapes. */
object Distance {
  var g_GJK_Iterations = 0

  // GJK using Voronoi regions (Christer Ericson) and region selection
  // optimizations (Casey Muratori).

  // The origin is either in the region of points[1] or in the edge region. The origin is
  // not in region of points[0] because that is the old point.
  def processTwo(p1s: Array[Vector2f], p2s: Array[Vector2f], points: Array[Vector2f]): (Int,Vector2f,Vector2f) = {
    // If in point[1] region
    val r = -points(1)
    var d = points(0) - points(1)

    val length = d.length
    d /= length // normalize
    var λ = r ∙ d
    if (λ <= 0f || length < ε) {
      // The simplex is reduced to a point.
      p1s(0) = p1s(1)
      p2s(0) = p2s(1)
      points(0) = points(1)
      (1, p1s(1), p2s(1))
    } else {
      // Else in edge region
      λ /= length
      (2, p1s(1) + λ * (p1s(0) - p1s(1)), p2s(1) + λ * (p2s(0) - p2s(1)))
    }
  }

  // Possible regions:
  // - points[2]
  // - edge points[0]-points[2]
  // - edge points[1]-points[2]
  // - inside the triangle
  def processThree(p1s: Array[Vector2f], p2s: Array[Vector2f], points: Array[Vector2f]): (Int,Vector2f,Vector2f) = {
    val Array(a,b,c) = points
    val ab = b - a
    val ac = c - a
    val bc = c - b

    val sn = -(a ∙ ab)
    val sd = (b ∙ ab)
    val tn = -(a ∙ ac)
    val td = (c ∙ ac)
    val un = -(b ∙ bc)
    val ud = (c ∙ bc)

    // In vertex c region?
    if (td <= 0.0f && ud <= 0.0f) {
      // Single point
      p1s(0) = p1s(2)
      p2s(0) = p2s(2)
      points(0) = points(2)
      return (1, p1s(2), p2s(2))
    }

    // Should not be in vertex a or b region.
    //B2_NOT_USED(sd);
    //B2_NOT_USED(sn);
    assert(sn > 0.0f || tn > 0.0f)
    assert(sd > 0.0f || un > 0.0f)

    val n = (ab × ac)

    // Should not be in edge ab region.
    val vc = n * (a × b)
    assert(vc > 0.0f || sn > 0.0f || sd > 0.0f)

    // In edge bc region?
    val va = n * (b × c)
    if (va <= 0.0f && un >= 0.0f && ud >= 0.0f && (un+ud) > 0.0f) {
      assert(un + ud > 0.0f)
      val λ = un / (un + ud)
      p1s(0) = p1s(2)
      p2s(0) = p2s(2)
      points(0) = points(2)
      return (2, p1s(1) + λ * (p1s(2) - p1s(1)), p2s(1) + λ * (p2s(2) - p2s(1)))
    }

    // In edge ac region?
    val vb = n * (c × a)
    if (vb <= 0.0f && tn >= 0.0f && td >= 0.0f && (tn+td) > 0.0f) {
      assert(tn + td > 0.0f)
      val λ = tn / (tn + td)
      p1s(1) = p1s(2)
      p2s(1) = p2s(2)
      points(1) = points(2)
      return (2, p1s(0) + λ * (p1s(2) - p1s(0)), p2s(0) + λ * (p2s(2) - p2s(0)))
	}

    // Inside the triangle, compute barycentric coordinates
    var denom = va + vb + vc
    assert(denom > 0.0f)
    denom = 1.0f / denom
    val u = va * denom
    val v = vb * denom
    val w = 1.0f - u - v
    (3, u * p1s(0) + v * p1s(1) + w * p1s(2), u * p2s(0) + v * p2s(1) + w * p2s(2))
  }

  def inPoints(w: Vector2f, points: Array[Vector2f], pointCount: Int): Boolean = {
    val k_tolerance = 100.0f * ε
    for (i <- 0 until pointCount) {
      val d = (w - points(i)).abs 
      val m = Vector2f.max(w.abs, points(i).abs)

      if (d.x < k_tolerance * (m.x + 1.0f) && d.y < k_tolerance * (m.y + 1.0f)) {
        return true
      }
    }

    return false
  }

	
  /**
   * Distance between any two objects that implement SupportsGenericDistance.
   * Note that x1 and x2 are passed so that they may store results - they must
   * be instantiated before being passed, and the contents will be lost.
   * 
   * @param x1 Set to closest point on shape1 (result parameter)
   * @param x2 Set to closest point on shape2 (result parameter)
   * @param shape1 Shape to test
   * @param xf1 Transform of shape1
   * @param shape2 Shape to test
   * @param xf2 Transform of shape2
   * @return
   */
  def distanceGeneric(shape1: SupportsGenericDistance, xf1: Transform2f,
                      shape2: SupportsGenericDistance, xf2: Transform2f): (Float,Vector2f,Vector2f) = {
    val p1s, p2s, points = new Array[Vector2f](3)
    var pointCount = 0

    var x1 = shape1.getFirstVertex(xf1)
    var x2 = shape2.getFirstVertex(xf2)

    val vSqr = 0.0f
    val maxIterations = 20
    for (iter <- 0 until maxIterations) {
      var v = x2 - x1
      val w1 = shape1.support(xf1, v)
      val w2 = shape2.support(xf2, -v)

      var vSqr = v ∙ v
      val w = w2 - w1
      val vw = v ∙ w
      if (vSqr - vw <= 0.01f * vSqr || inPoints(w, points, pointCount)) // or w in points
      {
        if (pointCount == 0) {
          x1 = w1
          x2 = w2
        }
        g_GJK_Iterations = iter
        return (MathUtil.sqrt(vSqr), x1, x2)
      }

      pointCount match {
        case 0 =>
          p1s(0) = w1
          p2s(0) = w2
          points(0) = w
          x1 = p1s(0)
          x2 = p2s(0)
          pointCount += 1
        case 1 =>
          p1s(1) = w1
          p2s(1) = w2
          points(1) = w
          val res = processTwo(p1s, p2s, points)
          pointCount = res._1
          x1 = res._2
          x2 = res._3
        case 2 => 
          p1s(2) = w1
          p2s(2) = w2
          points(2) = w
          val res = processThree(p1s, p2s, points)
          pointCount = res._1
          x1 = res._2
          x2 = res._3
      }

      // If we have three points, then the origin is in the corresponding triangle.
      if (pointCount == 3) {
        g_GJK_Iterations = iter
        return (0.0f, x1, x2)
      }

      var maxSqr = -Float.MaxValue
      for (i <- 0 until pointCount) {
        maxSqr = MathUtil.max(maxSqr, points(i) ∙ points(i))
      }

      if (pointCount == 3 || vSqr <= 100.0f * ε * maxSqr) {
        g_GJK_Iterations = iter;
        v = x2 - x1
        vSqr = v ∙ v

        return (MathUtil.sqrt(vSqr), x1, x2)
      }
    }

    g_GJK_Iterations = maxIterations;
    return (MathUtil.sqrt(vSqr), x1, x2)
  }

  def distanceCC(circle1: Circle, xf1: Transform2f,
                 circle2: Circle, xf2: Transform2f): (Float,Vector2f,Vector2f) = {
    val p1 = (xf1 * circle1.pos)
    val p2 = (xf2 * circle2.pos)

    var d = p2 - p1
    val dSqr = d ∙ d
    val r1 = circle1.radius - Settings.toiSlop
    val r2 = circle2.radius - Settings.toiSlop
    val r = r1 + r2
    if (dSqr > r * r) {
      val dLen = d.length
      d /= dLen // normalize
      val distance = dLen - r
      (distance, p1 + r1 * d, p2 - r2 * d)
    } else if (dSqr > ε * ε) {
      d = d.normalize
      val x = p1 + r1 * d 
      (0, x, x)
    } else {
      (0, p1, p1)
    }
  }

  // GJK is more robust with polygon-vs-point than polygon-vs-circle.
  // So we convert polygon-vs-circle to polygon-vs-point.
  def distancePC(polygon: Polygon, xf1: Transform2f,
                 circle: Circle, xf2: Transform2f): (Float,Vector2f,Vector2f) = {
    val point = Point(xf2 * circle.pos)

    var (distance, x1, x2) = distanceGeneric(polygon, xf1, point, Transform2f.Identity)

    val r = circle.radius - Settings.toiSlop

    if (distance > r) {
      distance -= r
      val d = (x2 - x1).normalize
      x2 -= r * d
    } else {
      distance = 0.0f
      x2 = x1
    }
    (distance, x1, x2)
  }

  /** 
   * Find the closest distance between shapes shape1 and shape2, 
   * and load the closest points into x1 and x2.
   * Note that x1 and x2 are passed so that they may store results - they must
   * be instantiated before being passed, and the contents will be lost.
   * 
   * @param x1 Closest point on shape1 is put here (result parameter)
   * @param x2 Closest point on shape2 is put here (result parameter)
   * @param shape1 First shape to test
   * @param xf1 Transform of first shape
   * @param shape2 Second shape to test
   * @param xf2 Transform of second shape
   */
  def distance(shape1: Shape, xf1: Transform2f,
               shape2: Shape, xf2: Transform2f): (Float, Vector2f, Vector2f) = (shape1, shape2) match {
    case (c1: Circle, c2: Circle) => distanceCC(c1, xf1, c2, xf2)
    case (p: Polygon, c: Circle) => distancePC(p, xf1, c, xf2)
    case (c: Circle, p: Polygon) =>
      val res = distancePC(p, xf2, c, xf1)
      // swap X1 and X2
      (res._1, res._3, res._2)
    case (p1: Polygon, p2: Polygon) => distanceGeneric(p1, xf1, p2, xf2)
    case _ => (0f, Vector2f.Zero, Vector2f.Zero)
  }

}

