package org.villane.box2d.collision

import vecmath._
import vecmath.Preamble._
import shapes._

object PolygonEdgeCollider extends Collider[Polygon, Edge] {

  def collide(polygon: Polygon, xf1: Transform2,
              edge: Edge, xf2: Transform2): Option[Manifold] = {
    val v1 = xf2 * edge.v1
    val v2 = xf2 * edge.v2
    val n = xf2.rot * edge.normal
    val v1Local = xf1 ** v1
    val v2Local = xf1 ** v2
    val nLocal = xf1.rot ** n

    var separation1 = 0f
    var separationIndex1 = -1 // which normal on the poly found the shallowest depth?
    var separationMax1 = -Float.MaxValue // the shallowest depth of edge in poly
    var separation2 = 0f
    var separationIndex2 = -1 // which normal on the poly found the shallowest depth?
    var separationMax2 = -Float.MaxValue // the shallowest depth of edge in poly

    var separationMax = -Float.MaxValue // the shallowest depth of edge in poly
    var separationV1 = false // is the shallowest depth from edge's v1 or v2 vertex?
    var separationIndex = -1 // which normal on the poly found the shallowest depth?

    val vertexCount = polygon.vertexCount
    val vertices = polygon.vertices
    val normals = polygon.normals

    var enterStartIndex = -1 // the last poly vertex above the edge
    var enterEndIndex = -1 // the first poly vertex below the edge
    var exitStartIndex = -1 // the last poly vertex below the edge
    var exitEndIndex = -1 // the first poly vertex above the edge
    //int deepestIndex;

    // the "N" in the following variables refers to the edge's normal. 
    // these are projections of poly vertices along the edge's normal, 
    // a.k.a. they are the separation of the poly from the edge. 
    var prevSepN = 0.0f
    var nextSepN = 0.0f
    var enterSepN = 0.0f // the depth of enterEndIndex under the edge (stored as a separation, so it's negative)
    var exitSepN = 0.0f // the depth of exitStartIndex under the edge (stored as a separation, so it's negative)
    var deepestSepN = Float.MaxValue // the depth of the deepest poly vertex under the end (stored as a separation, so it's negative)

    // for each poly normal, get the edge's depth into the poly. 
    // for each poly vertex, get the vertex's depth into the edge. 
    // use these calculations to define the remaining variables declared above.
    prevSepN = (vertices(vertexCount - 1) - v1Local) dot nLocal

    for (i <- 0 until vertexCount) {
      separation1 = (v1Local - vertices(i)) dot normals(i)
      separation2 = (v2Local - vertices(i)) dot normals(i)
      if (separation2 < separation1) {
        if (separation2 > separationMax) {
          separationMax = separation2
          separationV1 = false
          separationIndex = i
        }
      } else {
        if (separation1 > separationMax) {
          separationMax = separation1
          separationV1 = true
          separationIndex = i
        }
      }
      if (separation1 > separationMax1) {
        separationMax1 = separation1
        separationIndex1 = i
      }
      if (separation2 > separationMax2) {
        separationMax2 = separation2
        separationIndex2 = i
      }

      nextSepN = (vertices(i) - v1Local) dot nLocal
      if (nextSepN >= 0.0f && prevSepN < 0.0f) {
        exitStartIndex = if (i == 0) vertexCount - 1 else i - 1
        exitEndIndex = i
        exitSepN = prevSepN
      } else if (nextSepN < 0.0f && prevSepN >= 0.0f) {
        enterStartIndex = if (i == 0) vertexCount - 1 else i - 1
        enterEndIndex = i
        enterSepN = nextSepN
      }
      if (nextSepN < deepestSepN) {
        deepestSepN = nextSepN;
        //deepestIndex = i;
      }
      prevSepN = nextSepN
    }

    if (enterStartIndex == -1) {
      // poly is entirely below or entirely above edge, return with no contact:
      return None
    }
    if (separationMax > 0.0f) {
      // poly is laterally disjoint with edge, return with no contact:
      return None
    }

    // if the poly is near a convex corner on the edge
    if ((separationV1 && edge.corner1Convex) || (!separationV1 && edge.corner2Convex)) {
      // if shallowest depth was from edge into poly, 
      // use the edge's vertex as the contact point:
      if (separationMax > deepestSepN + Settings.linearSlop) {
        // if -normal angle is closer to adjacent edge than this edge, 
        // let the adjacent edge handle it and return with no contact:
        if (separationV1) {
          val temp = xf1.rot ** (xf2.rot * edge.corner1Dir)
          if ((normals(separationIndex1) dot temp) >= 0.0f) {
            return None
          }
        } else {
          val temp = xf1.rot ** (xf2.rot * edge.corner2Dir)
          if ((normals(separationIndex2) dot temp) <= 0.0f) {
            return None
          }
        }

        val id = ContactID(0, separationIndex, ContactID.NullFeature, false)
        val normal = xf1.rot * normals(separationIndex)
        val (p1, p2) = if (separationV1)
          (v1Local, edge.v1)
        else
          (v2Local, edge.v2)
        val points = new Array[ManifoldPoint](1)
        points(0) = ManifoldPoint(p1, p2, separationMax, id)
        return Some(Manifold(points, normal))
      }
    }

    // We're going to use the edge's normal now.
    val normal = -n

    // Check whether we only need one contact point.
    if (enterEndIndex == exitStartIndex) {
      val id = ContactID(0, enterEndIndex, ContactID.NullFeature, false)
      val p1 = vertices(enterEndIndex)
      val p2 = xf1 * p1
      val points = new Array[ManifoldPoint](1)
      points(0) = ManifoldPoint(p1, p2, enterSepN, id)
      return Some(Manifold(points, normal))
    }

    // dirLocal should be the edge's direction vector, but in the frame of the polygon.
    val dirLocal = -nLocal.normal // TODO: figure out why this optimization didn't work
    //Vec2 dirLocal = XForm.mulT(xf1.R, XForm.mul(xf2.R, edge.GetDirectionVector()));

    val dirProj1 = dirLocal dot (vertices(enterEndIndex) - v1Local)
    var dirProj2 = 0.0f

    // The contact resolution is more robust if the two manifold points are 
    // adjacent to each other on the polygon. So pick the first two poly
    // vertices that are under the edge:

    exitEndIndex = if (enterEndIndex == vertexCount - 1) 0 else enterEndIndex + 1
    if (exitEndIndex != exitStartIndex) {
      exitStartIndex = exitEndIndex
      exitSepN = nLocal dot (vertices(exitStartIndex) - v1Local)
    }
	dirProj2 = dirLocal dot (vertices(exitStartIndex) - v1Local)

    val points = new Array[ManifoldPoint](2)
    val id1 = ContactID(0, enterEndIndex, ContactID.NullFeature, false)

    val edgeLen = edge.length
    if (dirProj1 > edgeLen) {
      val ratio = (edgeLen - dirProj2) / (dirProj1 - dirProj2)
      val sep = if (ratio > 100.0f * Settings.Epsilon && ratio < 1.0f)
        exitSepN * (1.0f - ratio) + enterSepN * ratio
      else
        enterSepN
      points(0) = ManifoldPoint(v2Local, edge.v2, sep, id1)
    } else {
      val p1 = vertices(enterEndIndex)
      val p2 = xf2 ** (xf1 * vertices(enterEndIndex))
      points(0) = ManifoldPoint(p1, p2, enterSepN, id1)
    }

    val id2 = ContactID(0, exitStartIndex, ContactID.NullFeature, false)

    if (dirProj2 < 0.0f) {
      val ratio = (-dirProj1) / (dirProj2 - dirProj1)
      val sep = if (ratio > 100.0f * Settings.Epsilon && ratio < 1.0f)
        enterSepN * (1.0f - ratio) + exitSepN * ratio
      else
        exitSepN
      points(1) = ManifoldPoint(v1Local, edge.v1, sep, id2)
    } else {
      val p1 = vertices(exitStartIndex)
      val p2 = xf2 ** (xf1 * vertices(exitStartIndex))
      points(1) = ManifoldPoint(p1, p2, exitSepN, id2)
    }
    Some(Manifold(points, normal))
  }

}
