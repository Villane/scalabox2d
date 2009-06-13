package org.villane.box2d.shapes

import vecmath._
import vecmath.Preamble._
import collision._
import dynamics._

object Shape {
  def create(defn: ShapeDef) = defn match {
    case d: CircleDef => new Circle(d.pos, d.radius)
    case d: PointDef => new Point(d.pos, d.mass)
    case d: PolygonDef => new Polygon(d)
  }
}

/**
 * A shape is used for collision detection.
 */
abstract class Shape {
  /** Sweep radius relative to the parent body's center of mass. */
  def radius: Float

  /**
   * Test a point for containment in this shape. This only works for convex shapes.
   * @param t the shape world transform.
   * @param p a point in world coordinates.
   * @return true if the point is within the shape
   */
  def testPoint(t: Transform2, p: Vector2): Boolean
  /**
   * Perform a ray cast against this shape.
   * @param t world transform
	/// @param segment defines the begin and end point of the ray cast.
	/// @param maxLambda a number typically in the range [0,1].
   * @param lambda returns the hit fraction. You can use this to compute the contact point
	/// p = (1 - lambda) * segment.p1 + lambda * segment.p2.
	/// @param normal returns the normal at the contact point. If there is no intersection, the normal
	/// is not set.
   */
  def testSegment(t: Transform2, segment: Segment, maxLambda: Float): SegmentCollide

  def computeAABB(t: Transform2): AABB
  def computeMass(density: Float): Mass
  /**
   * Compute the volume and centroid of this shape intersected with a half plane
   * @param normal the surface normal
   * @param offset the surface offset along normal
   * @param xf the shape transform
   * @return the total volume less than offset along normal and the centroid
   */
  def computeSubmergedArea(normal: Vector2, offset: Float, t: Transform2): (Float, Vector2)

  def computeSweepRadius(pivot: Vector2): Float

}
