package org.villane.box2d.dynamics.contacts

import vecmath._
import shapes._
import collision._

/**
 * Contact between two circles. 
 */
case class CircleContact(c1: Circle, c2: Circle) extends Contact(c1, c2) with SingleManifoldContact {

  def evaluate(listener: ContactListener) {
    val b1 = shape1.body
    val b2 = shape2.body
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
