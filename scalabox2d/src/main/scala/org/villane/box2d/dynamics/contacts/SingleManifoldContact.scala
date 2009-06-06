package org.villane.box2d.dynamics.contacts

import vecmath.Vector2f
import collision.Manifold
import collision.ManifoldPoint

/**
 * Reusable implementation for contacts between convex shapes i.e. contacts with single manifolds. 
 */
trait SingleManifoldContact extends Contact {
  var manifoldHolder: Option[Manifold] = None
  def manifolds = if (manifoldHolder.isEmpty) Nil else manifoldHolder.get :: Nil

  def notifyListener(listener: ContactListener, mp: ManifoldPoint, normal: Vector2f, event: EventType) {
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
