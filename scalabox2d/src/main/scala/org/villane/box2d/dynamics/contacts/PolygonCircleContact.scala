package org.villane.box2d.dynamics.contacts

import shapes._
import collision._

/**
 * Contact between a polygon and a circle. 
 */
case class PolygonCircleContact(f1: Fixture, f2: Fixture) extends Contact(f1, f2)
  with MultiPointSingleManifoldContact {

  def collide = PolygonCircleCollider.collide(
    f1.shape.asInstanceOf[Polygon], f1.body.transform,
    f2.shape.asInstanceOf[Circle], f2.body.transform
  )

}