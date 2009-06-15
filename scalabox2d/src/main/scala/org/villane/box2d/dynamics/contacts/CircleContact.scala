package org.villane.box2d.dynamics.contacts

import vecmath._
import shapes._
import collision._

/**
 * Contact between two circles.
 */
case class CircleContact(f1: Fixture, f2: Fixture) extends Contact(f1, f2)
  with SingleManifoldContact {

  def collide = CircleCollider.collide(
    f1.shape.asInstanceOf[Circle], f1.body.transform,
    f2.shape.asInstanceOf[Circle], f2.body.transform
  )

}
