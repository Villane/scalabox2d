package org.villane.box2d.dynamics.contacts

import vecmath._
import shapes._
import collision._

/**
 * Contact between two circles. 
 */
case class CircleContact(f1: Fixture, f2: Fixture) extends Contact(f1, f2) with SingleManifoldContact {

  def c1 = f1.shape.asInstanceOf[Circle]
  def c2 = f2.shape.asInstanceOf[Circle]

  def evaluate(listener: ContactListener) {
    val b1 = fixture1.body
    val b2 = fixture2.body
    evaluate(listener, () => CircleCollider.collideCircles(c1, b1.transform, c2, b2.transform))
  }

  def evaluate(listener: ContactListener, collider: () => Option[Manifold]) {
    val oldMH = manifoldHolder
    manifoldHolder = collider()
    (oldMH, manifoldHolder) match {
      case (None, None) =>
      case (None, Some(manifold)) =>
        val mp = manifold.points(0) 
        mp.normalImpulse = 0f
        mp.tangentImpulse = 0f
        notifyListener(listener, mp, manifold.normal, EventType.Add)
      case (Some(m0), Some(manifold)) =>
        val mp = manifold.points(0) 
        val mp0 = m0.points(0)
        mp.normalImpulse = mp0.normalImpulse
        mp.tangentImpulse = mp0.tangentImpulse
        notifyListener(listener, mp, manifold.normal, EventType.Persist)
      case (Some(m0), None) => 
        notifyListener(listener, m0.points(0), m0.normal, EventType.Remove)
    }
  }

}
