package org.villane.box2d.dynamics.contacts

import shapes._
import collision._

/**
 * Contact between a polygon and a circle. 
 */
case class PolygonCircleContact(polygon: Polygon, circle: Circle) extends Contact(polygon, circle) with MultiPointSingleManifoldContact {

  def evaluate(listener: ContactListener) {
    val b1 = shape1.body
    val b2 = shape2.body

    // Match old contact ids to new contact ids and copy the
    // stored impulses to warm start the solver.
    evaluate(listener, () => PolygonCircleCollider.collidePolygonAndCircle(
      polygon, b1.transform, circle, b2.transform))
  }

}