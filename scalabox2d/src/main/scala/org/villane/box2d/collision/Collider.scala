package org.villane.box2d.collision

import vecmath._
import shapes.Shape

/**
 * Base interface for specific colliders.
 */
trait Collider[S1 <: Shape, S2 <: Shape] {
  def collide(shape1: S1, t1: Transform2,
              shape2: S2, t2: Transform2): Option[Manifold]
}
