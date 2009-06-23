package org.villane.box2d.collision

import vecmath._
import shapes._

/**
 * Companion object for performing collision detection without knowing the
 * concrete shape or collider types.
 */
object Collider {

  /**
   * Collide two shapes for which a collider exists, given their world transforms.
   */
  def collide(shape1: Shape, t1: Transform2, shape2: Shape, t2: Transform2):
    Option[Manifold] = (shape1, shape2) match {
    case (s1: Circle, s2: Circle) =>
      CircleCollider.collide(s1, t1, s2, t2)
    case (s1: Polygon, s2: Circle) =>
      PolygonCircleCollider.collide(s1, t1, s2, t2)
    case (s1: Circle, s2: Polygon) => 
      PolygonCircleCollider.collide(s2, t2, s1, t1)
    case (s1: Polygon, s2: Polygon) =>
      PolygonCollider.collide(s1, t1, s2, t2)
    case (s1: Edge, s2: Circle) =>
      EdgeCircleCollider.collide(s1, t1, s2, t2)
    case (s1: Circle, s2: Edge) =>
      EdgeCircleCollider.collide(s2, t2, s1, t1)
    case (s1: Polygon, s2: Edge) =>
      PolygonEdgeCollider.collide(s1, t1, s2, t2)
    case (s1: Edge, s2: Polygon) =>
      PolygonEdgeCollider.collide(s2, t2, s1, t1)
    case (s1, s2) =>
      throw new IllegalArgumentException(
        "No collider exists for shapes (" +
          s1.getClass.getSimpleName + ", " +
          s2.getClass.getSimpleName + ")")
  }

  /**
   * Collide two shapes for which a collider exists, in the same bg system.
   */
  def collide(shape1: Shape, shape2: Shape): Option[Manifold] =
    collide(shape1, Transform2.Identity, shape2, Transform2.Identity)

}

/**
 * Base trait for specific shape colliders.
 * 
 * A collider returns Some(Manifold(...)) on contact or None on no contact.
 */
trait Collider[S1 <: Shape, S2 <: Shape] {

  /**
   * Collide two shapes, given their world transforms.
   */
  def collide(shape1: S1, t1: Transform2,
              shape2: S2, t2: Transform2): Option[Manifold]

  /**
   * Collide two shapes with coordinates in the same background system.
   */
  def collide(shape1: S1, shape2: S2): Option[Manifold] =
    collide(shape1, Transform2.Identity, shape2, Transform2.Identity)

}
