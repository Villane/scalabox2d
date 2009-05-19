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
      val mpIter = manifold.points.elements
      while (mpIter.hasNext) {
        val mp = mpIter.next
        mp.normalImpulse = 0.0f
        mp.tangentImpulse = 0.0f
        var found = false
        val id = new ContactID(mp.id.features)

        if (oldMH.isDefined) {
          val m0 = oldMH.get
          var j = 0
          val mp0Iter = m0.points.elements
          while (mp0Iter.hasNext) {
            val mp0 = mp0Iter.next
            if (!persisted(j) && !found) {
              //val mp0 = m0.points(j)

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
            j += 1
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
      val mp0Iter = m0.points.elements
      var i = 0
      while (mp0Iter.hasNext) {
        val mp = mp0Iter.next
        if (!persisted(i))
          notifyListener(listener, mp, m0.normal, EventType.Remove)
        i += 1
      }
    }
  }

}
