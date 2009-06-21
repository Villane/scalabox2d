package org.villane.box2d.dynamics

import shapes._
import vecmath._
import vecmath.Preamble._
import contacts._
import joints._

object BodyFlags {
  val frozen = 0x0002
  val island = 0x0004
  val sleep = 0x0008
  val allowSleep = 0x0010
  val bullet = 0x0020
  val fixedRotation = 0x0040
}

final class Body(bd: BodyDef, val world: World) {
  var flags = 0

  private[this] var static = false
  /** Is this body static (immovable)? */
  def isStatic = static
  def isDynamic = !static

  var fixtures: List[Fixture] = Nil
  var userData: AnyRef = null

  var sleepTime = 0f
  
 var mass = 0f
 var invMass = 0f
 var I = 0f
 def inertia = I
 var invI = 0f

  var linearDamping = 0f
  var angularDamping = 0f

  var contactList: List[ContactEdge] = Nil
  var jointList: List[JointEdge] = Nil
  
  private[this] var _transform = Transform2(bd.pos, Matrix22.rotation(bd.angle))
  def pos = _transform.pos
  /** The swept motion for CCD */
  var sweep = new Sweep  
  var linearVelocity = Vector2.Zero
  var angularVelocity = 0f
  var force = Vector2.Zero
  var torque = 0f

  def transform = _transform
  /**
   * Set the position of the body's origin and rotation (radians).
   * This breaks any contacts and wakes the other bodies.
   * @param position the new world position of the body's origin (not necessarily
   * the center of mass).
   * @param angle the new world rotation angle of the body in radians.
   * @return false if the movement put a shape outside the world. In this case the
   * body is automatically frozen.
   */
  def setTransform(pos: Vector2, angle: Float): Boolean = {
    assert(!world.lock)
    if (world.lock) return true
    if (isFrozen) return false

    _transform = Transform2(pos, angle)

    sweep.c = transform * sweep.localCenter
    sweep.c0 = sweep.c
    sweep.a0 = angle
    sweep.a = angle

    var freeze = false

    for (f <- fixtures if !freeze) {
      val inRange = f.synchronize(world.broadPhase, transform, transform)

      if (!inRange) {
        freeze = true
      }
    }

    if (freeze) {
      flags |= BodyFlags.frozen
      linearVelocity = Vector2.Zero
      angularVelocity = 0f
      for (f <- fixtures) {
        f.destroyProxy(world.broadPhase)
      }

      // Failure
      return false
    }

    // Success
    world.broadPhase.commit()

    return true
  }

  define(bd)
  def define(bd: BodyDef) {
    assert(!world.lock)

		if (bd.isBullet) flags |= BodyFlags.bullet
		if (bd.fixedRotation) flags |= BodyFlags.fixedRotation
		if (bd.allowSleep) flags |= BodyFlags.allowSleep
		if (bd.isSleeping) flags |= BodyFlags.sleep
		
		sweep.localCenter = bd.mass.center
		sweep.t0 = 1.0f
		sweep.a0 = bd.angle
        sweep.a = bd.angle;
        sweep.c = _transform * sweep.localCenter
		sweep.c0 = sweep.c
		
		linearDamping = bd.linearDamping;
		angularDamping = bd.angularDamping;
		
		mass = bd.mass.mass;
		
		if (mass > 0.0f) {
			invMass = 1.0f / mass;
		}
		
		if ((flags & BodyFlags.fixedRotation) == 0) {
			I = bd.mass.I;
		}
		
		if (I > 0.0f) {
			invI = 1.0f / I;
		}
		
		static = (invMass == 0.0f && invI == 0.0f)
		
		userData = bd.userData;
  }
  
  /** The current world rotation angle in radians. */
  def angle = sweep.a
  def angle_=(a: Float) {
    sweep.a = a
    synchronizeTransform()
  }

  /** Get a copy of the world position of the center of mass. */
  def worldCenter = sweep.c
  /** Get a copy of the local position of the center of mass. */
  def localCenter = sweep.localCenter

  def toWorldPoint(localPoint: Vector2) = _transform * localPoint
  def toWorldVector(localVector: Vector2) = _transform.rot * localVector
  def toLocalPoint(worldPoint: Vector2) = _transform ** worldPoint
  def toLocalVector(worldVector: Vector2) = _transform.rot ** worldVector
  
  /**
   * Get the world linear velocity of a world point attached to this body.
   * @param worldPoint a point in world coordinates.
   * @return the world velocity of a point.
   */
  def getLinearVelocityFromWorldPoint(worldPoint: Vector2) =
    linearVelocity + (angularVelocity × (worldPoint - sweep.c))

  /**
   * Get the world velocity of a local point.
   * @param localPoint a point in local coordinates.
   * @return the world velocity of a point.
   */
  def getLinearVelocityFromLocalPoint(localPoint: Vector2) =
    getLinearVelocityFromWorldPoint(toWorldPoint(localPoint))

  /**
   * Apply a force at a world point. If the force is not
   * applied at the center of mass, it will generate a torque and
   * affect the angular velocity. This wakes up the body.
   * @param force the world force vector, usually in Newtons (N).
   * @param point the world position of the point of application.
   */
  def applyForce(force: Vector2, point: Vector2) {
    if (isSleeping) wakeUp()
    this.force += force
    this.torque += (point - sweep.c) × force
  }

  /**
   * Apply a torque. This affects the angular velocity
   * without affecting the linear velocity of the center of mass.
   * This wakes up the body.
   * @param torque about the z-axis (out of the screen), usually in N-m.
   */
  def applyTorque(torque: Float) {
    if (isSleeping) wakeUp()
    this.torque += torque
  }

  /**
   * Apply an impulse at a point. This immediately modifies the velocity.
   * It also modifies the angular velocity if the point of application
   * is not at the center of mass. This wakes up the body.
   * @param impulse the world impulse vector, usually in N-seconds or kg-m/s.
   * @param point the world position of the point of application.
   */
  def applyImpulse(impulse: Vector2, point: Vector2) {
    if (isSleeping) wakeUp()
    linearVelocity += impulse * invMass
    angularVelocity += invI * ((point - sweep.c) × impulse)
  }

  /**
   * Creates a fixture and attaches it to this body.
   * @param def the fixture definition.
   * @warning This function is locked during callbacks.
   */
  def createFixture(defn: FixtureDef): Fixture = {
    assert(!world.lock)

    if (world.lock) {
      return null
    }

    val f = Fixture(this, defn)
    fixtures = f :: fixtures

    // Add the shape to the world's broad-phase.
    f.createProxy(world.broadPhase, _transform)

    return f
  }

  /** 
   * Compute the mass properties from the attached fixtures. You typically call this
   * after adding all the shapes. If you add or remove shapes later, you may want
   * to call this again. Note that this changes the center of mass position.
   */
  def computeMassFromShapes() {
    assert(world.lock == false);
	if (world.lock == true) return;

    // Compute mass data from shapes.  Each shape has its own density.
    mass = 0f
    invMass = 0f
    I = 0f
    invI = 0f

    var center = Vector2.Zero
    for (f <- fixtures) {
      val massData = f.computeMass()
      mass += massData.mass
      center += massData.center * massData.mass
      I += massData.I
    }

    // Compute center of mass, and shift the origin to the COM
    if (mass > 0f) {
      invMass = 1 / mass
      center *= invMass
    }

    if (I > 0f && (flags & BodyFlags.fixedRotation) == 0) {
      // Center the inertia about the center of mass
      I -= mass * (center ∙ center)
      assert(I > 0.0f)
      invI = 1 / I
    } else {
      I = 0f
      invI = 0f
    }

    // Move center of mass
    sweep.localCenter = center
    sweep.c = _transform * sweep.localCenter
    sweep.c0 = sweep.c
		
    val oldStatic = static
    static = (invMass == 0f && invI == 0f)

    // If the body type changed, we need to refilter the broad-phase proxies.
    if (oldStatic != static) {
      for (f <- fixtures) {
        f.refilterProxy(world.broadPhase, _transform)
      }
    }
  }

  /** Is this body treated like a bullet for continuous collision detection? */
  def isBullet = (flags & BodyFlags.bullet) == BodyFlags.bullet
  /** Is this body frozen? */
  def isFrozen = (flags & BodyFlags.frozen) == BodyFlags.frozen
  /** Is this body sleeping (not simulating). */
  def isSleeping = (flags & BodyFlags.sleep) == BodyFlags.sleep
  def isAllowSleeping = (flags & BodyFlags.allowSleep) == BodyFlags.allowSleep

  /** Set to false to prevent this body from sleeping due to inactivity. */
  def allowSleeping(flag: Boolean) = {
    if (flag) {
	  flags |= BodyFlags.allowSleep
    } else {
      flags &= ~BodyFlags.allowSleep
      wakeUp()
    }
  }

  /** For internal use only. */
  def synchronizeFixtures(): Boolean = {
    val rot = Matrix22.rotation(sweep.a0)
    val xf1 = new Transform2(sweep.c0 - (rot * sweep.localCenter), rot)

    var inRange = true
    val iter = fixtures.elements
    while (inRange && iter.hasNext) {
      val f = iter.next
      inRange = f.synchronize(world.broadPhase, xf1, _transform)
    }

    if (!inRange) {
      flags |= BodyFlags.frozen
      linearVelocity = Vector2.Zero
      angularVelocity = 0f
      for (f <- fixtures) {
        f.destroyProxy(world.broadPhase)
      }
      // Failure
      return false
    }

    // Success
    return true
  }

  /** For internal use only. */
  @inline def synchronizeTransform() {
    //sweep.c - (rot * sweep.localCenter)
    //val rot = sweep.a
    //val vx = sweep.c.x - rot * sweep.localCenter.x
    //val vy = sweep.c.y - rot * sweep.localCenter.y

    //m_xf.position.x = m_sweep.c.x - (m_xf.R.col1.x * v1.x + m_xf.R.col2.x * v1.y);
    //m_xf.position.y = m_sweep.c.y - (m_xf.R.col1.y * v1.x + m_xf.R.col2.y * v1.y);
    val r = Matrix22.rotation(sweep.a)
    val v1 = sweep.localCenter
    val vx = sweep.c.x - (r.a11 * v1.x + r.a12 * v1.y)
    val vy = sweep.c.y - (r.a21 * v1.x + r.a22 * v1.y)
    _transform = Transform2(Vector2(vx,vy), r)
  }

  /**
   * This is used to prevent connected bodies from colliding.
   * It may lie, depending on the collideConnected flag, so
   * it won't be very useful external to the engine.
   */
  def isConnected(other: Body): Boolean = {
    for (jn <- jointList) {
      if (jn.other == other) {
        //System.out.println("connected");
        return ! jn.joint.collideConnected
      }
    }
    return false;
  }

  /** For internal use only. */
  def advance(t: Float) {
    // Advance to the new safe time
    sweep.advance(t)
    sweep.c = sweep.c0
    sweep.a = sweep.a0
    synchronizeTransform()
  }
	
  /**
   * Put this body to sleep so it will stop simulating.
   * This also sets the velocity to zero.
   */
  def putToSleep() {
    flags |= BodyFlags.sleep
    sleepTime = 0f
    linearVelocity = Vector2.Zero
	angularVelocity = 0f
    force = Vector2.Zero
    torque = 0f
  }

  /** Wake up this body so it will begin simulating. */
  def wakeUp() {
    flags &= ~BodyFlags.sleep
    sleepTime = 0f
  }
}
