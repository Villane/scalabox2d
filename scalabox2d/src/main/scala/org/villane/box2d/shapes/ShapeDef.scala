package org.villane.box2d.shapes

import vecmath._
import vecmath.Preamble._

/**
 * Superclass for shape definitions.
 * 
 * Shape definitions are mutable to make them easier to use.
 */
sealed abstract class ShapeDef

/** A circle definition. */
final case class CircleDef(var pos: Vector2, var radius: Scalar) extends ShapeDef

/** Point definition. Like a 0-radius circle, but has mass */
final case class PointDef(var pos: Vector2, var mass: Scalar) extends ShapeDef

object EdgeChainDef {
  def apply(vertices: Vector2*): EdgeChainDef = apply(false, vertices.toArray)
  def loop(vertices: Vector2*): EdgeChainDef = apply(true, vertices.toArray)
}

/** A chain of connected edges */
final case class EdgeChainDef(
  /** Whether to create an extra edge between the first and last vertices. */
  var loop: Boolean,
  /** The vertices in local coordinates. */
  var vertices: Array[Vector2]
) extends ShapeDef

object PolygonDef {
  /**
   * Build vertices to represent an axis-aligned box.
   * @param hx the half-width.
   * @param hy the half-height.
   */
  def box(hx: Scalar, hy: Scalar): PolygonDef = apply(Array((-hx,-hy),(hx,-hy),(hx,hy),(-hx,hy)))

  /**
   * Build vertices to represent an oriented box.
   * @param hx the half-width.
   * @param hy the half-height.
   * @param center the center of the box in local coordinates.
   */
  def box(hx: Scalar, hy: Scalar, center: Vector2): PolygonDef = {
    val pd = box(hx, hy)
    val xf = Transform2(center, Matrix22.Identity)
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
  def box(hx: Scalar, hy: Scalar, center: Vector2, angle: Scalar): PolygonDef = {
    val pd = box(hx, hy)
    val xf = Transform2(center, angle)
    pd.vertices = pd.vertices.map(v => xf * v)
    pd
  }

}

/**
 * Convex polygon definition.
 * 
 * The vertices must be in CCW order for a right-handed coordinate system
 * with the z-axis coming out of the screen.
 */
final case class PolygonDef(
  /** The CCW vertices in local coordinates. */
  var vertices: Array[Vector2]
) extends ShapeDef
