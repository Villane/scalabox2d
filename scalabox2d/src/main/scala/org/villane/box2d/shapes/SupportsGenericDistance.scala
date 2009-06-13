package org.villane.box2d.shapes

import vecmath._

/**
 * A shape that implements this interface can be used in distance calculations
 * for continuous collision detection.  This does not remove the necessity of
 * specialized penetration calculations when CCD is not in effect, however.
 */
trait SupportsGenericDistance {
  def support(xf: Transform2, v: Vector2): Vector2
  def getFirstVertex(xf: Transform2): Vector2
}

trait SupportsNewDistance extends Shape {
  def vertex(index: Int) = Vector2.Zero
  def support(v: Vector2) = 0
}
