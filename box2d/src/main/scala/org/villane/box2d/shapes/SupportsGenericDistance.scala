package org.villane.box2d.shapes

import vecmath._

/**
 * A shape that implements this interface can be used in distance calculations
 * for continuous collision detection.  This does not remove the necessity of
 * specialized penetration calculations when CCD is not in effect, however.
 */
trait SupportsGenericDistance {
  def support(xf: Transform2f, v: Vector2f): Vector2f
  def getFirstVertex(xf: Transform2f): Vector2f
}
