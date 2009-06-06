package org.villane.box2d.shapes

import vecmath._
import vecmath.Preamble._
import settings.Settings
import settings.Settings.ε

object Polygon {
  def computeCentroid(vertices: Array[Vector2f]) = {
	val count = vertices.size
    assert(count >= 3)
	
    var c = Vector2f.Zero
    var area = 0f
	
	        // pRef is the reference point for forming triangles.
	        // It's location doesn't change the result (except for rounding error).
	        val pRef = Vector2f.Zero
	//    #if 0
	//        // This code would put the reference point inside the polygon.
	//        for (int32 i = 0; i < count; ++i)
	//        {
	//            pRef += vs[i];
	//        }
	//        pRef *= 1.0f / count;
	//    #endif
	
	        val inv3 = 1f / 3
	
	        for (i <- 0 until count) {
	            // Triangle vertices.
	            val p1 = pRef
	            val p2 = vertices(i)
	            val p3 = if (i + 1 < count) vertices(i+1) else vertices(0)
	
	            val e1 = p2 - p1
	            val e2 = p3 - p1
	
	            val D = e1 × e2
	
	            val triangleArea = 0.5f * D
	            area += triangleArea;
	
	            // Area weighted centroid
                c += (p1 + p2 + p3) * (triangleArea * inv3)
	        }
	
	        // Centroid
	        assert(area > ε)
	        c * (1 / area)
	    }
	
	// http://www.geometrictools.com/Documentation/MinimumAreaRectangle.pdf
	def computeOBB(vertices: Array[Vector2f]) = {
		val count = vertices.length
		assert(count <= Settings.maxPolygonVertices)
        val p = new Array[Vector2f](count + 1)
        
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
			val uy = Vector2f(-ux.y, ux.x)
			var lower = Vector2f(Float.MaxValue, Float.MaxValue)
			var upper = Vector2f(-Float.MaxValue, -Float.MaxValue)

			for (j <- 0 until count) {
				val d = p(j) - root
				val r = Vector2f(ux ∙ d, uy ∙ d)
				lower = min(lower, r);
				upper = max(upper, r);
			}

			val area = (upper.x - lower.x) * (upper.y - lower.y);
			if (area < 0.95f * minArea){
			  minArea = area
              val rot = Matrix2f(ux, uy) 
			  val center = (lower + upper) * 0.5f
              obb = OBB(rot, root + (rot * center), (upper - lower) * 0.5f)
			}
		}

		assert(minArea < Float.MaxValue)
        obb
	}
	
}

class Polygon(defn: PolygonDef) extends Shape with SupportsGenericDistance {
  // TODO can this be used in constructor?
  val vertexCount = defn.vertices.length
  assert(3 <= vertexCount && vertexCount <= Settings.maxPolygonVertices)

  val vertices = Array.fromFunction(defn.vertices)(vertexCount)
  val normals = computeNormals 
  val centroid = Polygon.computeCentroid(vertices)
  val coreVertices = computeCoreVertices

  val obb = Polygon.computeOBB(vertices)

  def computeNormals = {
    val ns = new Array[Vector2f](vertexCount)
    // Compute normals. Ensure the edges have non-zero length.
    for (i <- 0 until vertexCount) {
      val i1 = i
      val i2 = if (i + 1 < vertexCount) i + 1 else 0
      val edge = vertices(i2) - vertices(i1)
      assert(edge.lengthSquared > ε * ε)
      ns(i) = edge.tangent.normalize
    }
    ns
  }

  def computeCoreVertices = {
    // Create core polygon shape by shifting edges inward.
    // Also compute the min/max radius for CCD.
    val cvs = new Array[Vector2f](vertexCount)
    for (i <- 0 until vertexCount) {
      val i1 = if (i - 1 >= 0) i - 1 else vertexCount - 1
      val i2 = i

      val n1 = normals(i1)
      val n2 = normals(i2)
      val v = vertices(i) - centroid

      val d = Vector2f(n1 ∙ v - Settings.toiSlop, n2 ∙ v - Settings.toiSlop)

      // Shifting the edge inward by b2_toiSlop should
      // not cause the plane to pass the centroid.

      // Your shape has a radius/extent less than b2_toiSlop.
      assert(d.x >= 0f)
      assert(d.y >= 0f)
      val A = Matrix2f(n1.x, n1.y, n2.x, n2.y)
      cvs(i) = A.solve(d) + centroid
    }
    cvs
  }

  def computeSweepRadius(pivot: Vector2f) = {
	assert(vertices.length > 0)
    var sr = 0f
    coreVertices foreach { v =>
      sr = MathUtil.max(sr, MathUtil.distanceSquared(v, pivot))
    }
    MathUtil.sqrt(sr)
  }

  def testPoint(t: Transform2f, p: Vector2f): Boolean = {
    val pLocal = t.rot ** (p - t.pos)

   	for (i <- 0 until vertexCount) {
   	  val dot = normals(i) ∙ (pLocal - vertices(i))

      if (dot > 0.0f) {
    	return false
      }
    }
   	return true
  }

  def testSegment(t: Transform2f, lambda: Float, normal: Vector2f) {}

  def computeSubmergedArea(normal: Vector2f, offset: Float, t: Transform2f) =
    (0f,Vector2f.Zero)

  def computeAABB(t: Transform2f) = {
	val rot = t.rot * obb.rot
	val h = rot.abs * obb.extents
	val p = t.pos + (t.rot * obb.center)
    AABB(p - h, p + h)
  }

  def computeMass(density: Float) = {
	assert(vertexCount >= 3)

    var center = Vector2f.Zero
	var area = 0f
	var I = 0f

	// pRef is the reference point for forming triangles.
	// It's location doesn't change the result (except for rounding error).
	var pRef = Vector2f.Zero

	val k_inv3 = 1f / 3

	for (i <- 0 until vertexCount) {
		// Triangle vertices.
		val p1 = pRef
		val p2 = vertices(i)
		val p3 = if (i + 1 < vertexCount) vertices(i+1) else vertices(0)

		val e1 = p2 - p1
		val e2 = p3 - p1

		val D = e1 × e2

		val triangleArea = 0.5f * D
		area += triangleArea

		// Area weighted centroid
        center += ((p1 + p2 + p3) * (triangleArea * k_inv3))

		val (px, py) = p1.tuple
		val (ex1, ey1) = e1.tuple
		val (ex2, ey2) = e2.tuple

		val intx2 = k_inv3 * (0.25f * (ex1*ex1 + ex2*ex1 + ex2*ex2) + (px*ex1 + px*ex2)) + 0.5f*px*px
		val inty2 = k_inv3 * (0.25f * (ey1*ey1 + ey2*ey1 + ey2*ey2) + (py*ey1 + py*ey2)) + 0.5f*py*py

		I += D * (intx2 + inty2)
	}

	// Total mass
	val mass = density * area

	// Center of mass
	assert(area > ε)
	center *= (1.0f / area)
	
	// Inertia tensor relative to the local origin.
    Mass(mass, center, I * density)
  }

  /** Get the first vertex and apply the supplied transform. */
  def getFirstVertex(xf: Transform2f) = xf * coreVertices(0)

  /**
   * Get the support point in the given world direction.
   * Use the supplied transform.
   */
  def support(xf: Transform2f, d: Vector2f) = {
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

}
