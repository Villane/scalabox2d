package org.villane.box2d.collision

import vecmath._
import vecmath.Preamble._
import shapes._
import collision._

object PolygonCollider extends Collider[Polygon, Polygon] {

  private case class MaxSeparation(bestFaceIndex: Int, bestSeparation: Float)
  private case class ClipVertex(v: Vector2, id: ContactID)

  // Find edge normal of max separation on A - return if separating axis is
  // found
  // Find edge normal of max separation on B - return if separation axis is
  // found
  // Choose reference edge as min(minA, minB)
  // Find incident edge
  // Clip
  // The normal points from 1 to 2
  def collide(polyA: Polygon, xfA: Transform2,
              polyB: Polygon, xfB: Transform2): Option[Manifold] = {
    //testbed.PTest.debugCount++;
    
    val sepA = findMaxSeparation(polyA, xfA, polyB, xfB)
    if (sepA.bestSeparation > 0.0f) return None

    val sepB = findMaxSeparation(polyB, xfB, polyA, xfA)
    if (sepB.bestSeparation > 0.0f) return None

    val k_relativeTol = 0.98f
    val k_absoluteTol = 0.001f
    val (poly1, poly2, xf1, xf2, edge1, flip) = 
    // TODO_ERIN use "radius" of poly for absolute tolerance.
      if (sepB.bestSeparation > k_relativeTol * sepA.bestSeparation + k_absoluteTol)
        (polyB, polyA, xfB, xfA, sepB.bestFaceIndex, true)
      else
        (polyA, polyB, xfA, xfB, sepA.bestFaceIndex, false)

    val incidentEdge = findIncidentEdge(poly1, xf1, edge1, poly2, xf2)

    val count1 = poly1.vertexCount
    val vertices1 = poly1.vertices

    val v11 = vertices1(edge1)
    val v12 = if (edge1 + 1 < count1) vertices1(edge1 + 1) else vertices1(0)

    //val sideNormal = (xf1.rot * (v12 - v11)).normalize
    //val frontNormal = sideNormal.normal
    val dvx = v12.x - v11.x
    val dvy = v12.y - v11.y
    val tdvx = xf1.rot.a11 * dvx + xf1.rot.a12 * dvy  
    val tdvy = xf1.rot.a21 * dvx + xf1.rot.a22 * dvy  
    val dvl = sqrt(tdvx * tdvx + tdvy * tdvy)
    val snx = tdvx / dvl
    val sny = tdvy / dvl
    val fnx = sny
    val fny = -snx

    //v11 = XForm.mul(xf1, v11);
    //v12 = XForm.mul(xf1, v12);
    //val Vector2(v11x, v11y) = xf1 * v11
    //val Vector2(v12x, v12y) = xf1 * v12
    val v11x = xf1.pos.x + xf1.rot.a11 * v11.x + xf1.rot.a12 * v11.y
    val v11y = xf1.pos.y + xf1.rot.a21 * v11.x + xf1.rot.a22 * v11.y
    val v12x = xf1.pos.x + xf1.rot.a11 * v12.x + xf1.rot.a12 * v12.y 
    val v12y = xf1.pos.y + xf1.rot.a21 * v12.x + xf1.rot.a22 * v12.y

    val frontOffset = fnx * v11x + fny * v11y
    val sideOffset1 = -(snx * v11x + sny * v11y)
    val sideOffset2 = snx * v12x + sny * v12y

    // Clip incident edge against extruded edge1 side edges.

    // Clip to box side 1
    val clipPoints1 = clipSegmentToLine(incidentEdge, -snx, -sny, sideOffset1)
    if (clipPoints1.length < 2) return None

    // Clip to negative box side 1
    val clipPoints2 = clipSegmentToLine(clipPoints1, snx, sny, sideOffset2)
    val cp2len = clipPoints2.length
    if (cp2len < 2) return None

    // Now clipPoints2 contains the clipped points.

    val points = new collection.mutable.ListBuffer[ManifoldPoint]
    for (cp2 <- clipPoints2) {
      val separation = (fnx * cp2.v.x + fny * cp2.v.y) - frontOffset

      if (separation <= 0.0f) {
        // should not inline this xf ** v creates just one vector anyway
        points += ManifoldPoint(
          xfA ** cp2.v,
          xfB ** cp2.v,
          separation,
          if (flip == cp2.id.flip) cp2.id else cp2.id withFlip flip
        )
      }
    }
    if (points.length == 0)
      None
    else
      Some(Manifold(points.toList, if (flip) -Vector2(fnx,fny) else Vector2(fnx,fny)))
  }

  private def clipSegmentToLine(vIn: List[ClipVertex],
                                normalx: Float, normaly: Float,
                                offset: Float): List[ClipVertex] = (vIn: @unchecked) match {
  case vIn0 :: vIn1 :: _ =>
    // Calculate the distance of end points to the line
    val distance0 = (normalx * vIn0.v.x + normaly * vIn0.v.y) - offset
    val distance1 = (normalx * vIn1.v.x + normaly * vIn1.v.y) - offset

    val vOut = new collection.mutable.ListBuffer[ClipVertex]
    // If the points are behind the plane
    if (distance0 <= 0.0f) {
      vOut += vIn0
    }
    if (distance1 <= 0.0f) {
      vOut += vIn1
    }

    // If the points are on different sides of the plane
    if (distance0 * distance1 < 0.0f) {
      // Find intersection point of edge and plane
      val interp = distance0 / (distance0 - distance1)
      val id = if (distance0 > 0.0f) vIn0.id else vIn1.id
      vOut += ClipVertex(vIn0.v + interp * (vIn1.v - vIn0.v), id)
    }

    vOut.toList
  }

  def edgeSeparation(poly1: Polygon, xf1: Transform2, edge1: Int, 
                     poly2: Polygon, xf2: Transform2): Float = {

    val count1 = poly1.vertexCount
    val vertices1 = poly1.vertices
    val normals1 = poly1.normals

    val count2 = poly2.vertexCount
    val vertices2 = poly2.vertices

    assert(0 <= edge1 && edge1 < count1)

    // Convert normal from poly1's frame into poly2's frame.
    //val normal1World = xf1.rot * normals1(edge1)
    //val normal1 = xf2.rot ** normal1World
    val n1e = normals1(edge1)
    val n1wx = xf1.rot.a11 * n1e.x + xf1.rot.a12 * n1e.y
    val n1wy = xf1.rot.a21 * n1e.x + xf1.rot.a22 * n1e.y
    val n1x = xf2.rot.a11 * n1wx + xf2.rot.a21 * n1wy
    val n1y = xf2.rot.a12 * n1wx + xf2.rot.a22 * n1wy

    // Find support vertex on poly2 for -normal.
    var index = 0
    var minDot = Float.MaxValue
    var i = 0
    while (i < count2) {
      //val dot = vertices2(i) ∙ normal1
      val dot = vertices2(i).x * n1x + vertices2(i).y * n1y
      if (dot < minDot) {
        minDot = dot
        index = i
      }
      i += 1
    }

    //val v1 = xf1 * vertices1(edge1)
    //val v2 = xf2 * vertices2(index)
    val v1e = vertices1(edge1)
    val v2e = vertices2(index)
    val v1x = xf1.pos.x + xf1.rot.a11 * v1e.x + xf1.rot.a12 * v1e.y
    val v1y = xf1.pos.y + xf1.rot.a21 * v1e.x + xf1.rot.a22 * v1e.y
    val v2x = xf2.pos.x + xf2.rot.a11 * v2e.x + xf2.rot.a12 * v2e.y
    val v2y = xf2.pos.y + xf2.rot.a21 * v2e.x + xf2.rot.a22 * v2e.y
    val dvx = v2x - v1x
    val dvy = v2y - v1y
    //(v2 - v1) ∙ normal1World
    dvx * n1wx + dvy * n1wy
  }

  // Find the max separation between poly1 and poly2 using face normals
  // from poly1.
  private def findMaxSeparation(poly1: Polygon, xf1: Transform2,
                        poly2: Polygon, xf2: Transform2): MaxSeparation = {
    val count1 = poly1.vertexCount
    val normals1 = poly1.normals

    val v = poly1.centroid
    val v1 = poly2.centroid

    // Vector pointing from the centroid of poly1 to the centroid of poly2.
    //val d = (xf2 * v1) - (xf1 * v)
    //val dLocal1 = xf1.rot ** d
    val dx = (xf2.pos.x + xf2.rot.a11 * v1.x + xf2.rot.a12 * v1.y) -
             (xf1.pos.x + xf1.rot.a11 * v.x + xf1.rot.a12 * v.y)
    val dy = (xf2.pos.y + xf2.rot.a21 * v1.x + xf2.rot.a22 * v1.y) -
             (xf1.pos.y + xf1.rot.a21 * v.x + xf1.rot.a22 * v.y)
    val dlx = xf1.rot.a11 * dx + xf1.rot.a21 * dy
    val dly = xf1.rot.a12 * dx + xf1.rot.a22 * dy

    // Find edge normal on poly1 that has the largest projection onto d.
    var edge = 0
    var maxDot = -Float.MaxValue
    var i = 0
    while (i < count1) {
      //val dot = normals1(i) ∙ dLocal1
      val dot = normals1(i).x * dlx + normals1(i).y * dly
      if (dot > maxDot) {
        maxDot = dot
        edge = i
      }
      i += 1
    }

    // Get the separation for the edge normal.
    var s = edgeSeparation(poly1, xf1, edge, poly2, xf2)
    if (s > 0.0f){
      return MaxSeparation(0, s)
    }

    // Check the separation for the previous edge normal.
    val prevEdge = if (edge - 1 >= 0) edge - 1 else count1 - 1
    val sPrev = edgeSeparation(poly1, xf1, prevEdge, poly2, xf2)
    if (sPrev > 0.0f) {
      return MaxSeparation(0, sPrev)
    }

    val nextEdge = if (edge + 1 < count1) edge + 1 else 0
    val sNext = edgeSeparation(poly1, xf1, nextEdge, poly2, xf2)
    if (sNext > 0.0f){
      return MaxSeparation(0, sNext)
    }

    // Find the best edge and the search direction.
    var (bestEdge, bestSeparation, increment) = 
      if (sPrev > s && sPrev > sNext) {
        (prevEdge, sPrev, -1)
      } else if (sNext > s){
        (nextEdge, sNext, 1)
      } else {
        return MaxSeparation(edge, s)
      }

    // Perform a local search for the best edge normal.
    var loop = true
    while (loop) {
      if (increment == -1)
        edge = if (bestEdge - 1 >= 0) bestEdge - 1 else count1 - 1
      else
        edge = if (bestEdge + 1 < count1) bestEdge + 1 else 0

      s = edgeSeparation(poly1, xf1, edge, poly2, xf2)
      if (s > 0.0f) {
        return MaxSeparation(0, s)
      }

      if (s > bestSeparation) {
        bestEdge = edge
        bestSeparation = s
      } else {
        loop = false
      }
    }

    MaxSeparation(bestEdge, bestSeparation)
  }

  private def findIncidentEdge(poly1: Polygon, xf1: Transform2, edge1: Int,
                               poly2: Polygon, xf2: Transform2) = {
    val count1 = poly1.vertexCount
    val normals1 = poly1.normals

    val count2 = poly2.vertexCount
    val vertices2 = poly2.vertices
    val normals2 = poly2.normals

    assert(0 <= edge1 && edge1 < count1)

    // Get the normal of the reference edge in poly2's frame.
    val normal1 = xf2.rot ** (xf1.rot * normals1(edge1))

    // Find the incident edge on poly2.
    var index = 0
    var minDot = Float.MaxValue
    var i = 0
    while (i < count2) {
      val dot = normal1 ∙ normals2(i)
      if (dot < minDot) {
        minDot = dot
        index = i
      }
      i += 1
    }

    // Build the clip vertices for the incident edge.
    val i1 = index
    val i2 = if (i1 + 1 < count2) i1 + 1 else 0

    List(
      ClipVertex(xf2 * vertices2(i1),
                 ContactID(edge1, i1, 0, false)),
      ClipVertex(xf2 * vertices2(i2),
                 ContactID(edge1, i2, 1, false))
    )
  }

}
