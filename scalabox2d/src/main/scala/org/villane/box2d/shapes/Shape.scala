package org.villane.box2d.shapes

import vecmath._
import vecmath.Preamble._
import collision._
import dynamics._

object Shape {
  def create(defn: ShapeDef) = defn match {
    case cd: CircleDef => new Circle(cd)
    case pd: PolygonDef => new Polygon(pd)
    case pd: PointDef => new Point(pd)
  }
}

/**
 * A shape is used for collision detection.
 */
abstract class Shape {
  /** Sweep radius relative to the parent body's center of mass. */
  var radius = 0f

  /**
   * Test a point for containment in this shape. This only works for convex shapes.
   * @param t the shape world transform.
   * @param p a point in world coordinates.
   * @return true if the point is within the shape
   */
  def testPoint(t: Transform2f, p: Vector2f): Boolean
  def testSegment(t: Transform2f, lambda: Float, normal: Vector2f)

  def computeMass(density: Float): Mass
  /* INTERNALS BELOW */
  def computeAABB(t: Transform2f): AABB
  /**
   * Compute the volume and centroid of this shape intersected with a half plane
   * @param normal the surface normal
   * @param offset the surface offset along normal
   * @param xf the shape transform
   * @return the total volume less than offset along normal and the centroid
   */
  def computeSubmergedArea(normal: Vector2f, offset: Float, t: Transform2f): (Float, Vector2f)

  def computeSweepRadius(pivot: Vector2f): Float

}
