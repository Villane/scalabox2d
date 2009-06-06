package org.villane.box2d.dynamics.contacts

import shapes._
import collision._

/**
 * Contact between a polygon and a circle. 
 */
case class PolygonCircleContact(f1: Fixture, f2: Fixture) extends Contact(f1, f2) with MultiPointSingleManifoldContact {

  def polygon = fixture1.shape.asInstanceOf[Polygon]
  def circle = fixture2.shape.asInstanceOf[Circle]

  def evaluate(listener: ContactListener) {
    val b1 = fixture1.body
    val b2 = fixture2.body

    // Match old contact ids to new contact ids and copy the
    // stored impulses to warm start the solver.
    evaluate(listener, () => PolygonCircleCollider.collidePolygonAndCircle(
      polygon, b1.transform, circle, b2.transform))
  }

}