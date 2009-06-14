package org.villane.box2d.collision

import vecmath._
import vecmath.Preamble._
import shapes._
import collision._

/** Holder class used internally in CollidePoly. */
case class MaxSeparation(bestFaceIndex: Int, bestSeparation: Scalar)
case class ClipVertex(v: Vector2, id: ContactID)

object PolygonCollider {
  def clipSegmentToLine(vIn: List[ClipVertex],
                        normal: Vector2, offset: Scalar): List[ClipVertex] = (vIn: @unchecked) match {
  case vIn0 :: vIn1 :: _ =>
    // Calculate the distance of end points to the line
    val distance0 = (normal ∙ vIn0.v) - offset
    val distance1 = (normal ∙ vIn1.v) - offset

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
                     poly2: Polygon, xf2: Transform2): Scalar = {

    val count1 = poly1.vertexCount
    val vertices1 = poly1.vertices
    val normals1 = poly1.normals

    val count2 = poly2.vertexCount
    val vertices2 = poly2.vertices

    assert(0 <= edge1 && edge1 < count1)

    // Convert normal from poly1's frame into poly2's frame.
    val normal1World = xf1.rot * normals1(edge1)
    val normal1 = xf2.rot ** normal1World

    // Find support vertex on poly2 for -normal.
    var index = 0
    var minDot = Scalar.MaxValue
    var i = 0
    while (i < count2) {
      val dot = vertices2(i) ∙ normal1
      if (dot < minDot) {
        minDot = dot
        index = i
      }
      i += 1
    }

    val v1 = xf1 * vertices1(edge1)
    val v2 = xf2 * vertices2(index)
    val separation = (v2 - v1) ∙ normal1World
    separation
  }

  // Find the max separation between poly1 and poly2 using face normals
  // from poly1.
  def findMaxSeparation(poly1: Polygon, xf1: Transform2,
                        poly2: Polygon, xf2: Transform2): MaxSeparation = {
    val count1 = poly1.vertexCount
    val normals1 = poly1.normals

    val v = poly1.centroid
    val v1 = poly2.centroid

    // Vector pointing from the centroid of poly1 to the centroid of poly2.
    val d = (xf2 * v1) - (xf1 * v)
    val dLocal1 = xf1.rot ** d

    // Find edge normal on poly1 that has the largest projection onto d.
    var edge = 0
    var maxDot = -Scalar.MaxValue
    var i = 0
    while (i < count1) {
      val dot = normals1(i) ∙ dLocal1
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

    return MaxSeparation(bestEdge, bestSeparation)
  }

  def findIncidentEdge(poly1: Polygon, xf1: Transform2, edge1: Int,
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
    var minDot = Scalar.MaxValue
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
                 ContactID(Features(edge1, i1, 0, false))),
      ClipVertex(xf2 * vertices2(i2),
                 ContactID(Features(edge1, i2, 1, false)))
    )
  }

  // Find edge normal of max separation on A - return if separating axis is
  // found
  // Find edge normal of max separation on B - return if separation axis is
  // found
  // Choose reference edge as min(minA, minB)
  // Find incident edge
  // Clip

  // The normal points from 1 to 2
  def collidePolygons(polyA: Polygon, xfA: Transform2,
                      polyB: Polygon, xfB: Transform2): Option[Manifold] = {
    //testbed.PTest.debugCount++;
    
    val sepA = findMaxSeparation(polyA, xfA, polyB, xfB)
    if (sepA.bestSeparation > 0.0f) {
      return None
    }

    val sepB = findMaxSeparation(polyB, xfB, polyA, xfA)
    if (sepB.bestSeparation > 0.0f) {
      return None
    }

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

    val sideNormal = (xf1.rot * (v12 - v11)).normalize
    val frontNormal = sideNormal.tangent

    //v11 = XForm.mul(xf1, v11);
    //v12 = XForm.mul(xf1, v12);
    //val Vector2(v11x, v11y) = xf1 * v11
    //val Vector2(v12x, v12y) = xf1 * v12
    val v11x = xf1.pos.x + xf1.rot.a11 * v11.x + xf1.rot.a12 * v11.y
    val v11y = xf1.pos.y + xf1.rot.a21 * v11.x + xf1.rot.a22 * v11.y
    val v12x = xf1.pos.x + xf1.rot.a11 * v12.x + xf1.rot.a12 * v12.y 
    val v12y = xf1.pos.y + xf1.rot.a21 * v12.x + xf1.rot.a22 * v12.y

    val frontOffset = frontNormal.x * v11x + frontNormal.y * v11y
    val sideOffset1 = -(sideNormal.x * v11x + sideNormal.y * v11y)
    val sideOffset2 = sideNormal.x * v12x + sideNormal.y * v12y

    // Clip incident edge against extruded edge1 side edges.

    // Clip to box side 1
    val clipPoints1 = clipSegmentToLine(incidentEdge, -sideNormal, sideOffset1)
    if (clipPoints1.length < 2) {
      return None
    }

    // Clip to negative box side 1
    val clipPoints2 = clipSegmentToLine(clipPoints1, sideNormal, sideOffset2)
    val cp2len = clipPoints2.length 
    if (cp2len < 2) {
      return None
    }

    // Now clipPoints2 contains the clipped points.

    val points = new collection.mutable.ListBuffer[ManifoldPoint]
    for (cp2 <- clipPoints2) {
      val separation = (frontNormal ∙ cp2.v) - frontOffset

      if (separation <= 0.0f) {
        points += ManifoldPoint(
          xfA ** cp2.v,
          xfB ** cp2.v,
          separation,
          cp2.id withFlip flip
        )
      }
    }
    if (points.length == 0)
      None
    else
      Some(Manifold(points.toList, if (flip) -frontNormal else frontNormal))
  }
}
