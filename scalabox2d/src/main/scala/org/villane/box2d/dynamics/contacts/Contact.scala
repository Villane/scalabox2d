package org.villane.box2d.dynamics.contacts

import collection.mutable
import vecmath._
import vecmath.Preamble._
import shapes._
import collision._

object ContactFlags {
  val nonSolid = 0x0001
  val slow = 0x0002
  val island = 0x0004
  val toi = 0x0008
}

object Contact {
  def apply(f1: Fixture, f2: Fixture) = (f1.shape, f2.shape) match {
    case (s1: Circle, s2: Circle) =>
      new SingleManifoldContact(f1, f2, CircleCollider)
    case (s1: Polygon, s2: Circle) =>
      new MultiPointSingleManifoldContact(f1, f2, PolygonCircleCollider)
    case (s1: Circle, s2: Polygon) => 
      new MultiPointSingleManifoldContact(f2, f1, PolygonCircleCollider)
    case (s1: Polygon, s2: Polygon) =>
      new MultiPointSingleManifoldContact(f1, f2, PolygonCollider)
    case (s1: Edge, s2: Circle) =>
      new SingleManifoldContact(f1, f2, EdgeCircleCollider)
    case (s1: Circle, s2: Edge) =>
      new SingleManifoldContact(f2, f1, EdgeCircleCollider)
    case (s1: Polygon, s2: Edge) =>
      new MultiPointSingleManifoldContact(f1, f2, PolygonEdgeCollider)
    case (s1: Edge, s2: Polygon) =>
      new MultiPointSingleManifoldContact(f2, f1, PolygonEdgeCollider)
    case (s1, s2) =>
      // XXX ERKKI this was return null; and probably should be NullContact? 
      throw new IllegalArgumentException("Contact(" + s1.getClass.getSimpleName +
        ", " + s2.getClass.getSimpleName + ") not supported!")
  }

  def destroy(contact: Contact) {
    if (contact.manifolds.length > 0) {
      contact.fixture1.body.wakeUp()
      contact.fixture2.body.wakeUp()
    }
  }

}

/**
 * Base class for contacts between shapes.
 * @author ewjordan
 */
abstract class Contact(val fixture1: Fixture, val fixture2: Fixture) {
  /** Node for connecting bodies. */
  val node1 = if (fixture2 != null) ContactEdge(fixture2.body, this) else null

  /** Node for connecting bodies. */
  val node2 = if (fixture1 != null) ContactEdge(fixture1.body, this) else null

  // TODO ERKKI: mixing friction/restitution has been moved to the solver
  /** Combined friction */
  var friction =
    if (fixture1 == null || fixture2 == null) 0f
    else sqrt(fixture1.friction * fixture2.friction)
    // TODO Settings.mixFriction(fixture1.friction, fixture2.friction)

  /** Combined restitution */
  var restitution =
    if (fixture1 == null || fixture2 == null) 0f
    else max(fixture1.restitution, fixture2.restitution)
    // TODO Settings.mixRestitution(fixture1.restitution, fixture2.restitution)

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
    if (fixture1 != null && fixture2 != null) {
      if (fixture1.isSensor || fixture2.isSensor) {
        flags |= ContactFlags.nonSolid
      }
      //world = s1.body.world
      fixture1.body.contactList = node1 :: fixture1.body.contactList
      fixture2.body.contactList = node2 :: fixture2.body.contactList
    }
  }

  def update(listener: ContactListener) {
    val oldCount = manifolds.length
    evaluate(listener)
    val newCount = manifolds.length

    val body1 = fixture1.body
    val body2 = fixture2.body

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

  // ERKKI impl from circlecontact
  def computeTOI(sweep1: Sweep, sweep2: Sweep) = {
    val toiInput = TOIInput(
      sweep1,
      sweep2,
      fixture1.computeSweepRadius(sweep1.localCenter),
      fixture2.computeSweepRadius(sweep2.localCenter),
      Settings.linearSlop
    )
	TOI.timeOfImpact(toiInput, fixture1.shape, fixture2.shape)
  }

}
