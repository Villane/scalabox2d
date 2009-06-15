package org.villane.box2d.dynamics.contacts

import shapes._
import collision._

case class EdgeCircleContact(f1: Fixture, f2: Fixture) extends Contact(f1, f2)
  with SingleManifoldContact {

  def collide = EdgeCircleCollider.collide(
    f1.shape.asInstanceOf[Edge], f1.body.transform,
    f2.shape.asInstanceOf[Circle], f2.body.transform
  )

}
