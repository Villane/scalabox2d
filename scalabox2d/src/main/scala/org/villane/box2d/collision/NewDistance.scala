package org.villane.box2d.collision

import vecmath._
import vecmath.Preamble._
import shapes._
import Settings.ε

case class DistanceInput(
  tA: Transform2,
  tB: Transform2,
  useRadii: Boolean
)

case class DistanceOutput(
  /** closest point on shape1 */
  var pA: Vector2,
  /** closest point on shape2 */
  var pB: Vector2,
  var distance: Scalar,
  /** number of GJK iterations used */
  iterations: Scalar
)

class SimplexCache {
  /** length or area */
  var metric: Scalar = 0f
  var count = 0
  /** vertices on shape A */
  var indexA = new Array[Int](3)
  /** vertices on shape B */
  var indexB = new Array[Int](3)
}

/* GJK using Voronoi regions (Christer Ericson) and Barycentric coordinates. */

class SimplexVertex {
  /** support point in shapeA */
  var wA: Vector2 = null
  /** support point in shapeB */
  var wB: Vector2 = null
  /** wB - wA */
  var w: Vector2 = null
  /** barycentric coordinate for closest point */
  var a: Scalar = 0f
  /** wA index */
  var indexA = 0
  /** wB index */
  var indexB = 0
}

class Simplex {
  var count = 0
  var vertices = Array[SimplexVertex](new SimplexVertex, new SimplexVertex, new SimplexVertex)
  def v1 = vertices(0)
  def v1_=(v: SimplexVertex) = vertices(0) = v
  def v2 = vertices(1)
  def v2_=(v: SimplexVertex) = vertices(1) = v
  def v3 = vertices(2)
  def v3_=(v: SimplexVertex) = vertices(2) = v

  def readCache(cache: SimplexCache,
                shapeA: Shape with SupportsNewDistance, tA: Transform2,
                shapeB: Shape with SupportsNewDistance, tB: Transform2) {
    assert(0 <= cache.count && cache.count <= 3)

    // Copy data from cache.
    count = cache.count
    for (i <- 0 until count) {
      val v = new SimplexVertex
      v.indexA = cache.indexA(i)
      v.indexB = cache.indexB(i)
      val wALocal = shapeA.vertex(v.indexA)
      val wBLocal = shapeB.vertex(v.indexB)
      v.wA = tA * wALocal
      v.wB = tB * wBLocal
      v.w = v.wB - v.wA
      v.a = 0f
      vertices(i) = v
    }

    // Compute the new simplex metric, if it is substantially different than
    // old metric then flush the simplex.
    if (count > 1) {
      val metric1 = cache.metric
      val metric2 = computeMetric
      if (metric2 < 0.5f * metric1 || 2.0f * metric1 < metric2 || metric2 < ε) {
        // Reset the simplex.
        count = 0
      }
    }

    // If the cache is empty or invalid ...
    if (count == 0) {
      val v = vertices(0)
      v.indexA = 0
      v.indexB = 0
      val wALocal = shapeA.vertex(0)
      val wBLocal = shapeB.vertex(0)
      v.wA = tA * wALocal
      v.wB = tB * wBLocal
      v.w = v.wB - v.wA
      count = 1
    }
  }

  def writeCache(cache: SimplexCache) {
    cache.metric = computeMetric
    cache.count = count
    for (i <- 0 until count) {
      cache.indexA(i) = vertices(i).indexA
      cache.indexB(i) = vertices(i).indexB
    }
  }

  def computeClosestPoint = count match {
    case 1 => v1.w
    case 2 => v1.a * v1.w + v2.a * v2.w
    case 3 => Vector2.Zero
    case _ => throw new IllegalStateException("call to computeClosestPoint when count=" + count)
  }

  def computeWitnessPoints = count match {
    case 1 => (v1.wA, v1.wB)
    case 2 => (v1.a * v1.wA + v2.a * v2.wA, v1.a * v1.wB + v2.a * v2.wB)
    case 3 =>
      val p = (v1.a * v1.wA + v2.a * v2.wA + v3.a * v3.wA)
      (p, p)
    case _ => throw new IllegalStateException("call to computeWitnessPoints when count=" + count)
  }

  def computeMetric: Scalar = count match {
    case 1 => 0.0f
    case 2 => distance(v1.w, v2.w)
    case 3 => (v2.w - v1.w) cross (v3.w - v1.w)
    case _ => throw new IllegalStateException("call to computeMetric when count=" + count)
  }

// Solve a line segment using barycentric coordinates.
//
// p = a1 * w1 + a2 * w2
// a1 + a2 = 1
//
// The vector from the origin to the closest point on the line is
// perpendicular to the line.
// e12 = w2 - w1
// dot(p, e) = 0
// a1 * dot(w1, e) + a2 * dot(w2, e) = 0
//
// 2-by-2 linear system
// [1      1     ][a1] = [1]
// [w1.e12 w2.e12][a2] = [0]
//
// Define
// d12_1 =  dot(w2, e12)
// d12_2 = -dot(w1, e12)
// d12 = d12_1 + d12_2
//
// Solution
// a1 = d12_1 / d12
// a2 = d12_2 / d12
  def solve2: Unit = {
    val w1 = v1.w
    val w2 = v2.w
    val e12 = w2 - w1

    // w1 region
    val d12_2 = -(w1 dot e12)
    if (d12_2 <= 0.0f) {
      // a2 <= 0, so we clamp it to 0
      v1.a = 1.0f
      count = 1
      return;
    }

    // w2 region
    val d12_1 = (w2 dot e12)
    if (d12_1 <= 0.0f) {
      // a1 <= 0, so we clamp it to 0
      v2.a = 1.0f
      count = 1
      v1 = v2
      return
    }

    // Must be in e12 region.
    val inv_d12 = 1.0f / (d12_1 + d12_2)
    v1.a = d12_1 * inv_d12
    v2.a = d12_2 * inv_d12
    count = 2
  }

// Possible regions:
// - points[2]
// - edge points[0]-points[2]
// - edge points[1]-points[2]
// - inside the triangle
  def solve3: Unit = {
    val w1 = v1.w
    val w2 = v2.w
    val w3 = v3.w

    // Edge12
    // [1      1     ][a1] = [1]
    // [w1.e12 w2.e12][a2] = [0]
    // a3 = 0
    val e12 = w2 - w1
    val w1e12 = (w1 dot e12)
    val w2e12 = (w2 dot e12)
    val d12_1 = w2e12
    val d12_2 = -w1e12

    // Edge13
    // [1      1     ][a1] = [1]
    // [w1.e13 w3.e13][a3] = [0]
    // a2 = 0
    val e13 = w3 - w1
    val w1e13 = w1 dot e13
    val w3e13 = w3 dot e13
    val d13_1 = w3e13
    val d13_2 = -w1e13

    // Edge23
    // [1      1     ][a2] = [1]
    // [w2.e23 w3.e23][a3] = [0]
    // a1 = 0
    val e23 = w3 - w2
    val w2e23 = (w2 dot e23)
    val w3e23 = (w3 dot e23)
    val d23_1 = w3e23
    val d23_2 = -w2e23

	// Triangle123
	val n123 = (e12 cross e13)

	val d123_1 = n123 * (w2 cross w3)
    val d123_2 = n123 * (w3 cross w1)
    val d123_3 = n123 * (w1 cross w2)

    // w1 region
    if (d12_2 <= 0.0f && d13_2 <= 0.0f) {
      v1.a = 1.0f
      count = 1
      return
    }

    // e12
    if (d12_1 > 0.0f && d12_2 > 0.0f && d123_3 <= 0.0f) {
      val inv_d12 = 1.0f / (d12_1 + d12_2)
      v1.a = d12_1 * inv_d12
      v2.a = d12_1 * inv_d12
      count = 2
      return
    }

    // e13
    if (d13_1 > 0.0f && d13_2 > 0.0f && d123_2 <= 0.0f) {
      val inv_d13 = 1.0f / (d13_1 + d13_2)
      v1.a = d13_1 * inv_d13
      v3.a = d13_2 * inv_d13
      count = 2
      v2 = v3
      return
    }

    // w2 region
    if (d12_1 <= 0.0f && d23_2 <= 0.0f) {
      v2.a = 1.0f
      count = 1
      v1 = v2
      return
    }

    // w3 region
    if (d13_1 <= 0.0f && d23_1 <= 0.0f) {
      v3.a = 1.0f
      count = 1
      v1 = v3
      return
    }

    // e23
    if (d23_1 > 0.0f && d23_2 > 0.0f && d123_1 <= 0.0f) {
      val inv_d23 = 1.0f / (d23_1 + d23_2)
      v2.a = d23_1 * inv_d23
      v3.a = d23_2 * inv_d23
      count = 2
      v1 = v3
      return
    }

    // Must be in triangle123
    val inv_d123 = 1.0f / (d123_1 + d123_2 + d123_3)
    v1.a = d123_1 * inv_d123
    v2.a = d123_2 * inv_d123
    v3.a = d123_3 * inv_d123
    count = 3
  }

}

/**
 * Implementation of the new Box2D distance algorithms.
 * Will replace Distance once it is completed.
 */
object NewDistance {
  def distance(cache: SimplexCache, input: DistanceInput,
               shapeA: Shape with SupportsNewDistance,
               shapeB: Shape with SupportsNewDistance): DistanceOutput = {
    val tA = input.tA
    val tB = input.tB

    // Initialize the simplex.
    val simplex = new Simplex
    simplex.readCache(cache, shapeA, tA, shapeB, tB)

    // Get simplex vertices as an array.
    val vertices = simplex.vertices

    // These store the vertices of the last simplex so that we
    // can check for duplicates and prevent cycling.
    val lastA = new Array[Int](4)
    val lastB = new Array[Int](4)
    var lastCount = 0

    // Main iteration loop.
    var iter = 0
    val maxIterationCount = 20
    var loop = true
    while (iter < maxIterationCount && loop) {
      // Copy simplex so we can identify duplicates.
      lastCount = simplex.count
      for (i <- 0 until lastCount) {
        lastA(i) = vertices(i).indexA
        lastB(i) = vertices(i).indexB
      }

      simplex.count match {
        case 1 =>
        case 2 => simplex.solve2
        case 3 => simplex.solve3
      }

      // If we have 3 points, then the origin is in the corresponding triangle.
      if (simplex.count == 3) loop = false

      // Compute closest point.
      val p = simplex.computeClosestPoint
      val distanceSqr = p.lengthSquared

      // Ensure the search direction is numerically fit.
      if (distanceSqr < ε * ε) {
        // The origin is probably contained by a line segment
        // or triangle. Thus the shapes are overlapped.

        // We can't return zero here even though there may be overlap.
        // In case the simplex is a point, segment, or triangle it is difficult
        // to determine if the origin is contained in the CSO or very close to it.
        loop = false
      }

      // Compute a tentative new simplex vertex using support points.
      val vertex = vertices(simplex.count)
      vertex.indexA = shapeA.support(tA.rot ** p)
      vertex.wA = tA * shapeA.vertex(vertex.indexA)
      //val wBLocal;
      vertex.indexB = shapeB.support(tB.rot ** -p)
      vertex.wB = tB * shapeB.vertex(vertex.indexB)
      vertex.w = vertex.wB - vertex.wA

      // Iteration count is equated to the number of support point calls.
      iter += 1

      // Check for convergence.
      val lowerBound = p dot vertex.w
      val upperBound = distanceSqr
      val relativeTolSqr = 0.01f * 0.01f // 1:100
      if (upperBound - lowerBound <= relativeTolSqr * upperBound) {
        // Converged!
        loop = false
      }

      // Check for duplicate support points.
      var duplicate = false
      for (i <- 0 until lastCount if !duplicate) {
        if (vertex.indexA == lastA(i) && vertex.indexB == lastB(i))
          duplicate = true
      }

      // If we found a duplicate support point we must exit to avoid cycling.
      if (duplicate) loop = false

      // New vertex is ok and needed.
      simplex.count += 1
	}

    // Prepare output.
    val (pA, pB) = simplex.computeWitnessPoints
    val out = DistanceOutput(pA, pB, vecmath.Preamble.distance(pA, pB), iter)

    // Cache the simplex.
    simplex.writeCache(cache)

    // Apply radii if requested.
    if (input.useRadii) {
      val rA = shapeA.radius
      val rB = shapeB.radius

      if (out.distance > rA + rB && out.distance > ε) {
        // Shapes are still no overlapped.
        // Move the witness points to the outer surface.
        out.distance -= rA + rB
        val normal = (out.pB - out.pA).normalize
        out.pA += rA * normal
        out.pB -= rB * normal
      } else {
    	// Shapes are overlapped when radii are considered.
    	// Move the witness points to the middle.
    	val p = (out.pA + out.pB) * 0.5f
    	out.pA = p
    	out.pB = p
    	out.distance = 0f
      }
    }
    out
  }
}
