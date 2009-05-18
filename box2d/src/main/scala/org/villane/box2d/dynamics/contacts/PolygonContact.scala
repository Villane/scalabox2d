package org.villane.box2d.dynamics.contacts

import vecmath._
import shapes._
import collision._

/**
 * ERKKI Contact between two (Convex?) polygons 
 */
case class PolygonContact(p1: Polygon, p2: Polygon) extends Contact(p1,p2) with MultiPointSingleManifoldContact {
  def evaluate(listener: ContactListener) {
    val b1 = shape1.body
    val b2 = shape2.body

    evaluate(listener, () => PolygonCollider.collidePolygons(p1, b1.transform, p2, b2.transform))
  }
}
