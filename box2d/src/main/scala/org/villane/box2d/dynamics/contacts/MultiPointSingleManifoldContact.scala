package org.villane.box2d.dynamics.contacts

import collision._

trait MultiPointSingleManifoldContact extends SingleManifoldContact {

  def evaluate(listener: ContactListener, collider: () => Option[Manifold]) {
    val persisted = Array(false,false)
    val oldMH = manifoldHolder
    manifoldHolder = collider()
    if (manifoldHolder.isDefined) {
      val manifold = manifoldHolder.get
      // Match old contact ids to new contact ids and copy the
      // stored impulses to warm start the solver.
      for (i <- 0 until manifold.points.length) {
        val mp = manifold.points(i)
        mp.normalImpulse = 0.0f
        mp.tangentImpulse = 0.0f
        var found = false
        val id = new ContactID(mp.id.features)

        if (oldMH.isDefined) {
          val m0 = oldMH.get
          for (j <- 0 until m0.points.length) {
            if (!persisted(j) && !found) {
              val mp0 = m0.points(j)

              if (mp0.id == id) {
                persisted(j) = true
                mp.normalImpulse = mp0.normalImpulse
                mp.tangentImpulse = mp0.tangentImpulse

                // A persistent point.
                found = true

                // Report persistent point.
                notifyListener(listener, mp, manifold.normal, EventType.Persist)
              }
            }
          }
        }

        // Report added point.
        if (!found) {
          notifyListener(listener, mp, manifold.normal, EventType.Add)
        }
      }
    }
    if (oldMH.isDefined) {
      // Report removed points.
      val m0 = oldMH.get
      for (i <- 0 until m0.points.length) {
        if (!persisted(i))
          notifyListener(listener, m0.points(i), m0.normal, EventType.Remove)
      }
    }
  }

}
