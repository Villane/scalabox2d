package org.villane.box2d.shapes

import vecmath._
import vecmath.Preamble._
import collision._
import dynamics._

object Shape {
  def create(defn: ShapeDef) = defn match {
    case cd: CircleDef => new Circle(cd)
    case pd: PolygonDef => new Polygon(pd)
  }
}

/**
 * For any shape it's location in a background system is known.
 * 
 * TODO get rid of the dependency on Body if possible. It would be ideal if shapes could exist without them.
 */
abstract class Shape(defn: ShapeDef) {
  var body: Body = null

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

  /**
   * Test a point for containment in this shape. This only works for convex shapes.
   * @param t the shape world transform.
   * @param p a point in world coordinates.
   * @return true if the point is within the shape
   */
  def testPoint(t: Transform2f, p: Vector2f): Boolean
  def computeAABB(t: Transform2f): AABB
  def computeSweptAABB(t1: Transform2f, t2: Transform2f): AABB
  def computeMass(): Mass

  /* INTERNALS BELOW */
  /** Internal */
  def updateSweepRadius(center: Vector2f)

  /** Internal */
  def synchronize(broadPhase: BroadPhase, transform1: Transform2f, transform2: Transform2f): Boolean = {
    if (proxyId == PairManager.NullProxy) {	
      return false;
    }

    // Compute an AABB that covers the swept shape (may miss some rotation effect).
    val aabb = computeSweptAABB(transform1, transform2)
    //if (this.getType() == ShapeType.CIRCLE_SHAPE){
    //System.out.println("Sweeping: "+transform1+" " +transform2);
    //System.out.println("Resulting AABB: "+aabb);
    //}
    if (broadPhase.inRange(aabb)) {
      broadPhase.moveProxy(proxyId, aabb)
      return true
    } else {
      return false;
    }
  }

  /** Internal */
  def refilterProxy(broadPhase: BroadPhase, transform: Transform2f) {
    if (proxyId == PairManager.NullProxy) {
      return
    }

    broadPhase.destroyProxy(proxyId)

    val aabb = computeAABB(transform)

    if (broadPhase.inRange(aabb)) {
      proxyId = broadPhase.createProxy(aabb, this)
    } else {
      proxyId = PairManager.NullProxy
    }
  }

  /** Internal */
  def createProxy(broadPhase: BroadPhase, transform: Transform2f) {
    assert(proxyId == PairManager.NullProxy)

    val aabb = computeAABB(transform)

    val inRange = broadPhase.inRange(aabb)

    assert(inRange, "You are creating a shape outside the world box.")

    if (inRange) {
      proxyId = broadPhase.createProxy(aabb, this)
    } else {
      proxyId = PairManager.NullProxy
    }
  }

  /** Internal */
  def destroyProxy(broadPhase: BroadPhase) {
    if (proxyId != PairManager.NullProxy) {
      broadPhase.destroyProxy(proxyId)
      proxyId = PairManager.NullProxy
    }
  }
}
