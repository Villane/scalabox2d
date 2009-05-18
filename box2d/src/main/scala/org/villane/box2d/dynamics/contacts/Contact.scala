package org.villane.box2d.dynamics.contacts

import collection.mutable
import vecmath._
import shapes._
import collision._

object ContactFlags {
  val nonSolid = 0x0001
  val slow = 0x0002
  val island = 0x0004
  val toi = 0x0008
}

object Contact {
  def apply[S1 <: Shape, S2 <: Shape](shape1: S1, shape2: S2) = (shape1, shape2) match {
    case (s1: Circle, s2: Circle) => CircleContact(s1, s2)
    case (s1: Polygon, s2: Circle) => PolygonCircleContact(s1, s2)
    case (s1: Circle, s2: Polygon) => reverse(PolygonCircleContact(s2, s1))
    case (s1: Polygon, s2: Polygon) => PolygonContact(s1, s2) 
    // XXX ERKKI this was return null; and probably should be NullContact? 
    case _ => throw new IllegalArgumentException("No contact creator for given types")
  }

  private def reverse(c: Contact) = {
    // If the parameters order is swapped, the manifold normals need to be negated
    for (i <- 0 until c.manifolds.length) {
      // TODO make manifold immutable again
      val m = c.manifolds(i)
      m.normal = -m.normal
    }
    c
  }

  def destroy(contact: Contact) {
    if (contact.manifolds.length > 0) {
      contact.shape1.body.wakeUp()
      contact.shape2.body.wakeUp()
    }
  }

}

/**
 * Base class for contacts between shapes.
 * @author ewjordan
 */
abstract class Contact(val shape1: Shape, val shape2: Shape) {
  /** The parent world. */
  //var world: World = null // TODO getCurrentWOlrd?

  /** Node for connecting bodies. */
  val node1 = if (shape2 != null) ContactEdge(shape2.body, this) else null

  /** Node for connecting bodies. */
  val node2 = if (shape1 != null) ContactEdge(shape1.body, this) else null

  /** Combined friction */
  var friction = if (shape1 == null || shape2 == null) 0f else MathUtil.sqrt(shape1.friction * shape2.friction)
  /** Combined restitution */
  var restitution = if (shape1 == null || shape2 == null) 0f else MathUtil.max(shape1.restitution, shape2.restitution)

  var flags = 0
  var toi = 0f
	
  def evaluate(listener: ContactListener)

  /**
   * Get the number of manifolds. This is 0 or 1 between convex shapes.
   * This may be greater than 1 for convex-vs-concave shapes. Each
   * manifold holds up to two contact points with a shared contact normal.
   *
   * Get the manifold array.
   */
  def manifolds: Seq[Manifold]

  def solid = (flags & ContactFlags.nonSolid) == 0

  init
  
  def init {
    // This is mainly so that NullContract could be created!
    if (shape1 != null && shape2 != null) {
      if (shape1.isSensor || shape2.isSensor) {
        flags |= ContactFlags.nonSolid
      }
      //world = s1.body.world
      shape1.body.contactList = node1 :: shape1.body.contactList
      shape2.body.contactList = node2 :: shape2.body.contactList
    }
  }

  def update(listener: ContactListener) {
    val oldCount = manifolds.length
    evaluate(listener)
    val newCount = manifolds.length

    val body1 = shape1.body
    val body2 = shape2.body

    if (newCount == 0 && oldCount > 0) {
      body1.wakeUp()
      body2.wakeUp()
    }
    
    // Slow contacts don't generate TOI events.
    if (body1.isStatic || body1.isBullet || body2.isStatic || body2.isBullet) {
      flags &= ~ContactFlags.slow
    } else {
      flags |= ContactFlags.slow
    }
  }

}
