package org.villane.box2d.dynamics

import vecmath._
import vecmath.Preamble
import shapes._
import collision._

object Fixture {
  def apply(body: Body, defn: FixtureDef) = new Fixture(defn, body)
}

class Fixture(defn: FixtureDef, val body: Body) {
  val shape = Shape.create(defn.shapeDef)

  /** The shape's friction coefficient, usually in the range [0,1]. */
  var friction = defn.friction

  /** The shape's restitution (elasticity) usually in the range [0,1]. */
  var restitution = defn.restitution

  /** Sweep radius relative to the parent body's center of mass. */
  var sweepRadius = 0f

  /** The shape's density, usually in kg/m^2. */
  var density = defn.density

  var proxyId = PairManager.NullProxy

  /**
   * A sensor shape collects contact information but never generates a collision
   * response.
   */
  var isSensor = defn.isSensor

  /** Contact filtering data. */
  var filter = defn.filter

  /** Use this to store application specify shape data. */
  var userData = defn.userData

  def computeMass() = shape.computeMass(density)

  def computeSweepRadius(pivot: Vector2f) = shape.computeSweepRadius(pivot)

  /** Internal */
  def createProxy(broadPhase: BroadPhase, transform: Transform2f) {
    assert(proxyId == PairManager.NullProxy)

    val aabb = shape.computeAABB(transform)

    val inRange = broadPhase.inRange(aabb)

    assert(inRange, "You are creating a shape outside the world box.")

    proxyId = if (inRange)
      broadPhase.createProxy(aabb, this)
    else
      PairManager.NullProxy
  }

  /** Internal */
  def destroyProxy(broadPhase: BroadPhase) {
    if (proxyId != PairManager.NullProxy) {
      broadPhase.destroyProxy(proxyId)
      proxyId = PairManager.NullProxy
    }
    // ERKKI: box2d de-allocates the shape here
  }

  /** Internal */
  def synchronize(broadPhase: BroadPhase, transform1: Transform2f, transform2: Transform2f): Boolean = {
    if (proxyId == PairManager.NullProxy) return false

    // Compute an AABB that covers the swept shape (may miss some rotation effect).
    val aabb = shape.computeAABB(transform1) combineWith shape.computeAABB(transform2)
    if (broadPhase.inRange(aabb)) {
      broadPhase.moveProxy(proxyId, aabb)
      true
    } else {
      false
    }
  }

  /** Internal */
  def refilterProxy(broadPhase: BroadPhase, transform: Transform2f) {
    if (proxyId == PairManager.NullProxy) return

    broadPhase.destroyProxy(proxyId)

    val aabb = shape.computeAABB(transform)

    if (broadPhase.inRange(aabb)) {
      proxyId = broadPhase.createProxy(aabb, this)
    } else {
      proxyId = PairManager.NullProxy
    }
  }

}
