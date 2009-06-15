package org.villane.box2d.dynamics.contacts

import vecmath._
import shapes._
import collision._

/**
 * Contact between two convex polygons.
 */
case class PolygonContact(f1: Fixture, f2: Fixture) extends Contact(f1, f2)
  with MultiPointSingleManifoldContact {

  def collide = PolygonCollider.collide(
    f1.shape.asInstanceOf[Polygon], f1.body.transform,
    f2.shape.asInstanceOf[Polygon], f2.body.transform
  )

}
