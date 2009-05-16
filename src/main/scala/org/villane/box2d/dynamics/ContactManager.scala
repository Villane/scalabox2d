package org.villane.box2d.dynamics

import contacts._
import shapes._
import collision._

/** Delegate of World - for internal use. */
class ContactManager extends PairListener {
  var world: World = null

  val destroyImmediate = false

  def pairAdded(proxyUserData1: AnyRef, proxyUserData2: AnyRef): AnyRef = {
    val shape1 = proxyUserData1.asInstanceOf[Shape]
    val shape2 = proxyUserData2.asInstanceOf[Shape]

    val body1 = shape1.body
    val body2 = shape2.body

    if (body1.isStatic && body2.isStatic) {
      return NullContact
    }

    if (body1 eq body2) {
      return NullContact
    }
    
    if (body2.isConnected(body1)) {
      return NullContact
    }
    
    if (world.contactFilter != null && !world.contactFilter.shouldCollide(shape1, shape2)) {
      return NullContact
    }

    // Call the factory.
    val c = Contact(shape1, shape2)

    if (c == null) {
      return NullContact
    }

    // Insert into the world.
    world.contactList += c
    // world.contactCount += 1

    c
  }

  // This is a callback from the broadphase when two AABB proxies cease
  // to overlap. We retire the b2Contact.
  def pairRemoved(proxyUserData1: AnyRef, proxyUserData2: AnyRef, pairUserData: AnyRef) {
    //B2_NOT_USED(proxyUserData1);
    //B2_NOT_USED(proxyUserData2);

    if (pairUserData == null) {
      return
    }

    val c = pairUserData.asInstanceOf[Contact]
    if (c == NullContact) {
      return
    }

    // An attached body is being destroyed, we must destroy this contact
    // immediately to avoid orphaned shape pointers.
    destroy(c)
  }

  def destroy(c: Contact) {
    val shape1 = c.shape1
    val shape2 = c.shape2

    // Inform the user that this contact is ending.
    val manifoldCount = c.manifolds.length
    if (manifoldCount > 0 && (world.contactListener != null)) {
      val b1 = shape1.body
      val b2 = shape2.body
      for (manifold <- c.manifolds) {
        val normal = manifold.normal
        for (mp <- manifold.points) {
          val pos = b1.toWorldPoint(mp.localPoint1)
          val v1 = b1.getLinearVelocityFromLocalPoint(mp.localPoint1)
          val v2 = b2.getLinearVelocityFromLocalPoint(mp.localPoint2)
          val cp = ContactPoint(shape1, shape2, pos, v2 - v1, manifold.normal, mp.separation, c.friction, c.restitution, mp.id)
          world.contactListener.remove(cp)
        }
      }
    }

    // TODO XXX HACK This probably has horrible performance!
    // Contact lists should be optimized for random removal as well
    // Remove from the world.
    world.contactList -= c
    val body1 = shape1.body
    val body2 = shape2.body

    // Remove from body 1
    body1.contactList = body1.contactList.remove(c==)

    // Remove from body 2
    body2.contactList = body2.contactList.remove(c==)

    Contact.destroy(c)
    // world.contactCount -= 1
  }

  def collide() {
    // Update awake contacts.
    var iC = 0
    while (iC < world.contactList.length) {
      val c = world.contactList(iC)
      iC += 1

      val body1 = c.shape1.body
      val body2 = c.shape2.body
      if (!body1.isSleeping || !body2.isSleeping) {
        c.update(world.contactListener)
      }
    }
  }
}
