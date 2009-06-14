package org.villane.box2d.dynamics.contacts

import vecmath.Vector2
import collision.Manifold
import collision.ManifoldPoint

/**
 * Reusable implementation for contacts between convex shapes i.e. contacts with single manifolds. 
 */
trait SingleManifoldContact extends Contact {
  var manifoldHolder: Option[Manifold] = None
  def manifolds = if (manifoldHolder.isEmpty) Nil else manifoldHolder.get :: Nil

  def evaluate(listener: ContactListener) = evaluate(listener, collide)

  def collide: Option[Manifold]

  def evaluate(listener: ContactListener, manifold: Option[Manifold]) {
    val oldMH = manifoldHolder
    manifoldHolder = manifold
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

  final def notifyListener(listener: ContactListener, mp: ManifoldPoint, normal: Vector2, event: EventType) {
    if (listener != null) {
      val b1 = fixture1.body
      val b2 = fixture2.body
      val v1 = b1.getLinearVelocityFromLocalPoint(mp.localPoint1)
      val v2 = b2.getLinearVelocityFromLocalPoint(mp.localPoint2)
      val cp = ContactPoint(
        fixture1,
        fixture2,
        b1.toWorldPoint(mp.localPoint1),
        v2 - v1,
        normal,
        mp.separation,
        friction,
        restitution,
        mp.id
      )
      event match {
        case EventType.Add => listener.add(cp)
        case EventType.Persist => listener.persist(cp)
        case EventType.Remove => listener.remove(cp)
      }
    }
  }
  
}
