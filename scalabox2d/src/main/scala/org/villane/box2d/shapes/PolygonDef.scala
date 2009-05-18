package org.villane.box2d.shapes

import vecmath._
import vecmath.Preamble._

object PolygonDef {
  def apply(vertices: Vector2f*) = {
    val pd = new PolygonDef
    pd.vertices = vertices.toArray
    pd
  }

  /**
   * Build vertices to represent an axis-aligned box.
   * @param hx the half-width.
   * @param hy the half-height.
   */
  def box(hx: Float, hy: Float) = apply((-hx,-hy),(hx,-hy),(hx,hy),(-hx,hy))

  /**
   * Build vertices to represent an oriented box.
   * @param hx the half-width.
   * @param hy the half-height.
   * @param center the center of the box in local coordinates.
   */
  def box(hx: Float, hy: Float, center: Vector2f): PolygonDef = {
    val pd = box(hx, hy)
    val xf = Transform2f(center, Matrix2f.Identity)
    pd.vertices = pd.vertices.map(v => xf * v)
    pd
  }

  /**
   * Build vertices to represent an oriented box.
   * @param hx the half-width.
   * @param hy the half-height.
   * @param center the center of the box in local coordinates.
   * @param angle the rotation of the box in local coordinates.
   */
  def box(hx: Float, hy: Float, center: Vector2f, angle: Float): PolygonDef = {
    val pd = box(hx, hy)
    val xf = Transform2f(center, angle)
    pd.vertices = pd.vertices.map(v => xf * v)
    pd
  }

}

/**
 * Convex polygon. The vertices must be in CCW order for a right-handed
 * coordinate system with the z-axis coming out of the screen.
 * Add vertices using PolygonDef.add(Vector2f),
 * and create the polygon shape using Body::createShape(ShapeDef).
 */
class PolygonDef extends ShapeDef {
  /** 
   * The polygon vertices in local coordinates.
   * <BR><BR>
   * Accessing this field is discouraged - it remains
   * public for the moment, but that is likely to change.
   * Please use addVertex(Vec2) and getVertexList/Array
   * instead to add to or inspect the current vertices.
   */
  var vertices: Array[Vector2f] = Array()
}