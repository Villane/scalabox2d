package org.villane.box2d.shapes

import vecmath._
import vecmath.Preamble._
import Settings.ε

object Polygon {
  def computeCentroid(vertices: Array[Vector2]) = {
    var c = Vector2.Zero
    var area = 0f
    forTriangles(vertices) { t =>
      area += t.area
      // Area weighted centroid
      c += t.center
    }
    // Centroid
    assert(area > ε)
    c * (1 / area)
  }

  object Triangle {
    val inv3 = 1f / 3
    def apply(pRef: Vector2, vertices: Array[Vector2], p2index: Int): Triangle = apply(
      pRef,
      vertices(p2index),
      if (p2index + 1 < vertices.length) vertices(p2index + 1) else vertices(0)
    )
  }

  case class Triangle(p1: Vector2, p2: Vector2, p3: Vector2) {
    lazy val e1 = p2 - p1
    lazy val e2 = p3 - p1
    lazy val e3 = p3 - p2
    lazy val D = e1 × e2
    lazy val area = 0.5f * D
    lazy val center = area * Triangle.inv3 * (p1 + p2 + p3)
  }

  def forTriangles[T](vertices: Array[Vector2])(block: Triangle => T) = {
    val count = vertices.size
    assert(count >= 3)

    // pRef is the reference point for forming triangles.
    // It's location doesn't change the result (except for rounding error).
    val pRef = Vector2.Zero
    // This code would put the reference point inside the polygon.
    // for (int32 i = 0; i < count; ++i)
    // {
    //    pRef += vs[i];
    // }
    // pRef *= 1.0f / count;

    for (i <- 0 until count) {
      val triangle = Triangle(pRef, vertices, i)
      block(triangle)
    }
  }

  def computeOBB(vertices: Array[Vector2]) = {
    val count = vertices.length
    val p = new Array[Vector2](count + 1)
    for (i <- 0 until count) {
      p(i) = vertices(i)
    }
    p(count) = p(0)

    var minArea = Float.MaxValue
    var obb: OBB = null

    for (i <- 1 to count) {
      val root = p(i-1)
      var ux = p(i) - root
      val length = ux.length
      ux /= length // normalize
      assert(length > ε)
      val uy = Vector2(-ux.y, ux.x)
      var lower = Vector2(Float.MaxValue, Float.MaxValue)
      var upper = Vector2(-Float.MaxValue, -Float.MaxValue)

      for (j <- 0 until count) {
        val d = p(j) - root
        val r = Vector2(ux ∙ d, uy ∙ d)
        lower = min(lower, r)
        upper = max(upper, r)
      }

      val area = (upper.x - lower.x) * (upper.y - lower.y)
      if (area < 0.95f * minArea) {
        minArea = area
        val rot = Matrix22(ux, uy) 
        val center = (lower + upper) * 0.5f
        obb = OBB(rot, root + (rot * center), (upper - lower) * 0.5f)
      }
    }
    assert(minArea < Float.MaxValue)
    obb
  }
}

class Polygon(defn: PolygonDef) extends Shape with SupportsGenericDistance {
  val radius = Settings.polygonRadius
  val vertexCount = defn.vertices.length
  assert(3 <= vertexCount && vertexCount <= Settings.maxPolygonVertices)

  val vertices = Array.fromFunction(defn.vertices)(vertexCount)
  val normals = computeNormals 
  val centroid = Polygon.computeCentroid(vertices)

  def computeNormals = {
    val ns = new Array[Vector2](vertexCount)
    var i = 0
    // Compute normals. Ensure the edges have non-zero length.
    // TODO forEdges?
    Polygon.forTriangles(vertices) { t =>
      assert(t.e3.lengthSquared > ε * ε)
      ns(i) = t.e3.unit.normal
      i += 1
    }
    ns
  }

  def testPoint(t: Transform2, p: Vector2): Boolean = {
    val pLocal = t.rot ** (p - t.pos)
   	for (i <- 0 until vertexCount) {
   	  val dot = normals(i) ∙ (pLocal - vertices(i))
      if (dot > 0.0f) return false
    }
   	true
  }

  def testSegment(t: Transform2, segment: Segment, maxLambda: Float): SegmentCollide = {
    var lower = 0f
    var upper = maxLambda

    val p1 = t ** segment.p1
	val p2 = t ** segment.p2
	val d = p2 - p1
	var index = -1

    for (i <- 0 until vertexCount) {
      // p = p1 + a * d
      // dot(normal, p - v) = 0
      // dot(normal, p1 - v) + a * dot(normal, d) = 0
      val numerator = normals(i) dot (vertices(i) - p1)
      val denominator = normals(i) dot d

      if (denominator == 0.0f) {
        if (numerator < 0.0f) return SegmentCollide.Miss
      } else {
        // Note: we want this predicate without division:
        // lower < numerator / denominator, where denominator < 0
        // Since denominator < 0, we have to flip the inequality:
        // lower < numerator / denominator <==> denominator * lower > numerator.
        if (denominator < 0.0f && numerator < lower * denominator) {
          // Increase lower.
          // The segment enters this half-space.
          lower = numerator / denominator
          index = i
        } else if (denominator > 0.0f && numerator < upper * denominator) {
          // Decrease upper.
          // The segment exits this half-space.
          upper = numerator / denominator
        }
      }

      if (upper < lower) return SegmentCollide.Miss
    }

    assert(0f <= lower && lower <= maxLambda)

    if (index >= 0)
      SegmentCollide.hit(lower, t.rot * normals(index))
    else
      SegmentCollide.startsInside(0, Vector2.Zero)
  }

  val obb = Polygon.computeOBB(vertices)
  def computeAABB(t: Transform2) = {
	val rot = t.rot * obb.rot
	val h = rot.abs * obb.extents
	val p = t.pos + (t.rot * obb.center)
    AABB(p - h, p + h)
    /* THIS IS THE NEW AABB, WITHOUT OBB
    var lower = t * vertices(0)
    var upper = lower
    var i = 1
    while (i < vertices.size) {
      val v = t * vertices(i)
      lower = min(lower, v)
      upper = max(upper, v)
      i += 1
    }
    AABB(Vector2(lower.x - radius, lower.y - radius),
         Vector2(upper.x + radius, upper.y + radius))
    */
    // INLINED FROM
    //val vTrans = vertices map (t * _)
    //val lower = vTrans reduceLeft min
    //val upper = vTrans reduceLeft max
    //val r = (radius, radius)
    //AABB(lower-radius, upper+radius)
  }

  def computeMass(density: Float) = {
    var area = 0f
    var center = Vector2.Zero
    var I = 0f

    import Polygon.Triangle._
    Polygon.forTriangles(vertices) { t =>
      area += t.area
      // Area weighted centroid
      center += t.center

      val (px, py) = t.p1.tuple
      val (ex1, ey1) = t.e1.tuple
      val (ex2, ey2) = t.e2.tuple

      val intx2 = inv3 * (0.25f * (ex1*ex1 + ex2*ex1 + ex2*ex2) + (px*ex1 + px*ex2)) + 0.5f*px*px
      val inty2 = inv3 * (0.25f * (ey1*ey1 + ey2*ey1 + ey2*ey2) + (py*ey1 + py*ey2)) + 0.5f*py*py

      I += t.D * (intx2 + inty2)
    }

    // Total mass
    val mass = density * area

    // Center of mass
    assert(area > ε)
    center *= (1.0f / area)

    // Inertia tensor relative to the local origin.
    Mass(mass, center, I * density)
  }

  def computeSubmergedArea(normal: Vector2, offset: Float, t: Transform2):
    (Float, Vector2) = {
    //Transform plane into shape co-ordinates
    val normalL = t.rot ** normal
    val offsetL = offset - (normal dot t.pos)

    val depths = new Array[Float](vertexCount)
    var diveCount = 0
    var intoIndex = -1
    var outoIndex = -1

    var lastSubmerged = false
    for (i <- 0 until vertexCount) {
      depths(i) = (normalL dot vertices(i)) - offsetL
      val isSubmerged = depths(i) < -ε
      if (i > 0) {
        if (isSubmerged) {
          if (!lastSubmerged) {
            intoIndex = i-1
            diveCount += 1
          }
        } else {
          if (lastSubmerged) {
            outoIndex = i-1
            diveCount += 1
          }
        }
      }
      lastSubmerged = isSubmerged
    }

    diveCount match {
      case 0 =>
        if (lastSubmerged) {
          //Completely submerged
          val mass = computeMass(1)
          return (mass.mass, t * mass.center)
        } else {
          //Completely dry
          return (0, Vector2.Zero)
        }
      case 1 =>
        if (intoIndex == -1) {
          intoIndex = vertexCount - 1
        } else {
          outoIndex = vertexCount - 1
        }
    }

    val intoIndex2 = (intoIndex + 1) % vertexCount
	val outoIndex2 = (outoIndex + 1) % vertexCount

    val intoLambda = (0 - depths(intoIndex)) / (depths(intoIndex2) - depths(intoIndex))
    val outoLambda = (0 - depths(outoIndex)) / (depths(outoIndex2) - depths(outoIndex))

    val intoVec = Vector2(
      vertices(intoIndex).x * (1-intoLambda) + vertices(intoIndex2).x * intoLambda,
      vertices(intoIndex).y * (1-intoLambda) + vertices(intoIndex2).y * intoLambda
    )
    val outoVec = Vector2(
      vertices(outoIndex).x * (1-outoLambda) + vertices(outoIndex2).x * outoLambda,
      vertices(outoIndex).y * (1-outoLambda) + vertices(outoIndex2).y * outoLambda
    )

    // Initialize accumulator
    var area = 0f
    var center = Vector2.Zero
    var p2 = vertices(intoIndex2)
    var p3: Vector2 = null

    import Polygon.Triangle
    import Triangle.inv3

    // An awkward loop from intoIndex2+1 to outIndex2
    var i = intoIndex2
    while (i != outoIndex2) {
      i = (i + 1) % vertexCount
      p3 = if (i == outoIndex2) outoVec else vertices(i)

      // Add the triangle formed by intoVec,p2,p3
      val tri = Triangle(intoVec, p2, p3)
      area += tri.area
      center += tri.center
      p2 = p3
    }

    // Normalize and transform centroid
    center *= 1.0f / area
    (area, t * center)
  }

  def computeSweepRadius(pivot: Vector2) = {
    assert(vertices.length > 0)
    var sr = 0f
    // TODO NO CORE VERTICES!!!
    coreVertices foreach { v =>
      sr = max(sr, distanceSquared(v, pivot))
    }
    sqrt(sr)
  }

  /** Core vertices and everything below is deprecated */
  val coreVertices = computeCoreVertices
  def computeCoreVertices = {
    // Create core polygon shape by shifting edges inward.
    // Also compute the min/max radius for CCD.
    val cvs = new Array[Vector2](vertexCount)
    for (i <- 0 until vertexCount) {
      val i1 = if (i - 1 >= 0) i - 1 else vertexCount - 1
      val i2 = i

      val n1 = normals(i1)
      val n2 = normals(i2)
      val v = vertices(i) - centroid

      val d = Vector2(n1 ∙ v - Settings.toiSlop, n2 ∙ v - Settings.toiSlop)

      // Shifting the edge inward by b2_toiSlop should
      // not cause the plane to pass the centroid.

      // Your shape has a radius/extent less than b2_toiSlop.
      assert(d.x >= 0f)
      assert(d.y >= 0f)
      val A = Matrix22(n1.x, n1.y, n2.x, n2.y)
      cvs(i) = A.solve(d) + centroid
    }
    cvs
  }

  /**
   * Get the support point in the given world direction.
   * Use the supplied transform.
   */
  def support(xf: Transform2, d: Vector2) = {
    val dLocal = xf.rot ** d

    var bestIndex = 0
    var bestValue = coreVertices(0) ∙ dLocal
    for (i <- 1 until vertexCount) {
      val value = coreVertices(i) ∙ dLocal
      if (value > bestValue) {
        bestIndex = i
        bestValue = value
      }
    }
    xf * coreVertices(bestIndex) 
  }

  /** Get the first vertex and apply the supplied transform. */
  def getFirstVertex(t: Transform2) = t * coreVertices(0)

}
