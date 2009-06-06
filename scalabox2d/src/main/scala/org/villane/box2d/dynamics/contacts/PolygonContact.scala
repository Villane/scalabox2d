package org.villane.box2d.dynamics.contacts

import vecmath._
import shapes._
import collision._

/**
 * ERKKI Contact between two (Convex?) polygons 
 */
case class PolygonContact(f1: Fixture, f2: Fixture) extends Contact(f1, f2) with MultiPointSingleManifoldContact {
  def p1 = fixture1.shape.asInstanceOf[Polygon]
  def p2 = fixture2.shape.asInstanceOf[Polygon]

  def evaluate(listener: ContactListener) {
    val b1 = fixture1.body
    val b2 = fixture2.body

    evaluate(listener, () => PolygonCollider.collidePolygons(p1, b1.transform, p2, b2.transform))
  }
}
