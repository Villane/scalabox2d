package org.villane.box2d.dynamics

import vecmath._
import vecmath.Preamble._
import shapes._
import contacts._
import collision._
import joints._
import Settings.ε
import broadphase._
import controllers._

import java.util.concurrent._
import collection.jcl.ArrayList

/**
 * The world that physics takes place in.
 */
class World(val aabb: AABB, var gravity: Vector2, doSleep: Boolean) {
  var lock = false
  
  val contactManager = new ContactManager
  contactManager.world = this
  val broadPhase = new BroadPhase(aabb, contactManager)
  
  // TODO find out which kind of lists have the best performance
  
  val bodyList = new ArrayList[Body]
  /** Do not access, won't be useful! */
  val contactList = new ArrayList[Contact] 
  val jointList = new ArrayList[Joint]
  val controllers = new ArrayList[Controller]

  var allowSleep = doSleep
  val groundBody = createBody(new BodyDef)
  var positionIterationCount = 0
  
  /** Should we apply position correction? */
  var positionCorrection = true
  /** Should we use warm-starting?  Improves stability in stacking scenarios. */
  var warmStarting = true
  /** Should we enable continuous collision detection? */
  var continuousPhysics = true
  
  var contactFilter: ContactFilter = DefaultContactFilter
  var contactListener: ContactListener = null
  var destructionListener: DestructionListener = null
  var boundaryListener: BoundaryListener = null

  private var invDt0 = 0f
  private var postStepList = new ArrayList[Steppable]

  /** 
   * Create a body given a definition. No reference to the definition
   * is retained.  Body will be static unless mass is nonzero.
   * <BR><em>Warning</em>: This function is locked during callbacks.
   */
  def createBody(defn: BodyDef): Body = {
    assert(!lock)
    if (lock) {
      return null;
    }

    val b = new Body(defn, this)
    bodyList += b
	b
  }

  /**
   * Destroy a rigid body given a definition. No reference to the definition
   * is retained. This function is locked during callbacks.
   * <BR><em>Warning</em>: This automatically deletes all associated shapes and joints.
   * <BR><em>Warning</em>: This function is locked during callbacks.
   */
  def destroyBody(b: Body): Unit = {
    assert(!lock)
    if (lock) {
      return
    }

    // Delete the attached joints.
    for (jn <- b.jointList) {
      if (destructionListener != null) destructionListener.sayGoodbye(jn.joint)
      destroyJoint(jn.joint)
    }

    // Delete the attached shapes. This destroys broad-phase
    // proxies and pairs, leading to the destruction of contacts.
    for (f <- b.fixtures) {
      if (destructionListener != null) destructionListener.sayGoodbye(f)
      f.destroyProxy(broadPhase)
    }

    // Remove the body from controllers
    for (c <- b.managingControllers) c.removeBody(b)

    // Remove dependent controllers from the world
    for (c <- b.dependentControllers) {
      if (destructionListener != null) destructionListener.sayGoodbye(c)
      removeController(c)
    }

    bodyList -= b
  }
	
  /**
   * Create a joint to constrain bodies together. No reference to the definition
   * is retained. This may cause the connected bodies to cease colliding.
   * <BR><em>Warning</em> This function is locked during callbacks.
   */
  def createJoint(defn: JointDef): Joint = {
    assert(!lock)
    if (lock) {
      return null
    }
    val j = Joint.create(defn)

    // Connect to the world list.
    jointList += j

    // If the joint prevents collisions, then reset collision filtering
    if (!defn.collideConnected) {
      // Reset the proxies on the body with the minimum number of shapes.
      // ERKKI TODO List.length is expensive, use something else
      val b = if (defn.body1.fixtures.length < defn.body2.fixtures.length)
        defn.body1
      else
        defn.body2
      
      for (f <- b.fixtures) refilter(f)
    }

    return j
  }

  /**
   * Destroy a joint. This may cause the connected bodies to begin colliding.
   * <BR><em>Warning</em>: This function is locked during callbacks.
   */
  def destroyJoint(j: Joint) {
    assert(!lock)

	val collideConnected = j.collideConnected

    // Remove from the doubly linked list.
    jointList -= j

    // Disconnect from island graph.
	val body1 = j.body1
    val body2 = j.body2

    // Wake up connected bodies.
    body1.wakeUp()
    body2.wakeUp()

    // Remove from body 1
    body1.jointList = body1.jointList.remove(j.node1==) 

    // Remove from body 2
    body2.jointList = body2.jointList.remove(j.node2==) 

    // If the joint prevents collisions, then reset collision filtering.
    if (!collideConnected) {
      // Reset the proxies on the body with the minimum number of shapes.
      // TODO erkki list.length is expensive, use something else
      val b = if (body1.fixtures.length < body2.fixtures.length)
        body1
      else
        body2
      for (f <- b.fixtures) refilter(f)
    }
  }

  def addController(c: Controller) = controllers += c

  def removeController(c: Controller) = {
    c match {
      case ctrl: SelfManagedBodies =>
        for (b <- ctrl.bodies) b.managingControllers -= ctrl
      case _ =>
    }
    controllers -= c
  }

  /**
   * Take a time step. This performs collision detection, integration,
   * and constraint solution.
   * @param dt the amount of time to simulate, this should not vary.
   * @param iterations the number of iterations to be used by the constraint solver.
   */
  def step(dt: Float, iterations: Int) {
    lock = true
    val step = new TimeStep(dt)
    step.maxIterations = iterations
    step.positionCorrection = positionCorrection
    step.warmStarting = warmStarting
    step.dtRatio = invDt0 * dt

    // Update contacts.
    contactManager.collide()

    // Integrate velocities, solve velocity constraints, and integrate positions.
    if (step.dt > 0f) solve(step)

    // Handle TOI events.
    if (continuousPhysics && step.dt > 0f) solveTOI(step)

    if (step.dt > 0f) invDt0 = step.invDt
    lock = false

    postStep(dt, iterations)
  }
    
  /** Goes through the registered postStep functions and calls them. */
  private def postStep(dt: Float, iterations: Int) {
    for (s <- postStepList) {
      s.step(dt, iterations)
    }
  }
    
  /**
   * Registers a Steppable object to be stepped
   * immediately following the physics step, once
   * the locks are lifted.
   * @param s
   */
  def registerPostStep(s: Steppable) = postStepList += s
    
  /**
   * Unregisters a method from post-stepping.
   * Fails silently if method is not found.
   * @param s
   */
  def unregisterPostStep(s: Steppable) = postStepList -= s
    
  /**
   * Query the world for all shapes that potentially overlap the
   * provided AABB up to max count.
   * The number of shapes found is returned.
   * @param aabb the query box.
   * @param maxCount the capacity of the shapes array.
   * @return array of shapes overlapped, up to maxCount in length
   */
  def query(aabb: AABB, maxCount: Int): Array[Fixture] = {
    val objs = broadPhase.query(aabb, maxCount)
    objs.map(_.asInstanceOf[Fixture])
  }

  def proxyCount = broadPhase.proxyCount

  def pairCount = broadPhase.pairManager.pairCount

  //--------------- Internals Below -------------------
  // ERKKI Internals were public
	
  /** Re-filter a shape. This re-runs contact filtering on a shape. */
  private def refilter(fixture: Fixture) {
    fixture.refilterProxy(broadPhase, fixture.body.transform)
  }

  private def solve(step: TimeStep) = {
    controllers foreach (_.step(step))

    positionIterationCount = 0

    // Size the island for the worst case.
    var island = new Island(bodyList.size, contactList.size, jointList.size, contactListener)

    // Clear all the island flags.
    for (b <- bodyList) {
      b.flags &= ~BodyFlags.island
    }
    for (c <- contactList) {
      c.flags &= ~ContactFlags.island
    }
    for (j <- jointList) {
      j.islandFlag = false
    }

    // Build and simulate all awake islands.
    var stackSize = bodyList.length
    val stack = new Array[Body](stackSize)

    val futures = if (Settings.threadedIslandSolving)
      new collection.mutable.ListBuffer[Future[Int]]
    else
      null

    var iBody = 0
    while (iBody < bodyList.length) {
      val seed = bodyList(iBody)
      iBody += 1

      if (((seed.flags & (BodyFlags.island | BodyFlags.sleep | BodyFlags.frozen)) == 0) && !seed.isStatic) {

      // Reset island and stack.
      island.clear()
      var stackCount = 0
      stack(stackCount) = seed
      stackCount += 1
      seed.flags |= BodyFlags.island

      // Perform a depth first search (DFS) on the constraint graph.
      while (stackCount > 0) {
        // Grab the next body off the stack and add it to the island.
        stackCount -= 1
        val b = stack(stackCount)
        island.add(b)

        // Make sure the body is awake.
        b.flags &= ~BodyFlags.sleep

        // To keep islands as small as possible, we don't
        // propagate islands across static bodies.
        if (!b.isStatic) {
          // Search all contacts connected to this body.
          for (cn <- b.contactList) {
            // Has this contact already been added to an island?
            if ((cn.contact.flags & (ContactFlags.island | ContactFlags.nonSolid)) == 0 &&
              // Is this contact touching?
              !cn.contact.manifolds.isEmpty)
            {
              island.add(cn.contact)
              cn.contact.flags |= ContactFlags.island

              // Was the other body already added to this island?
              val other = cn.other
              if ((other.flags & BodyFlags.island) == 0) {
                assert(stackCount < stackSize)
                stack(stackCount) = other
                stackCount += 1
                other.flags |= BodyFlags.island
              }
            }
          }

          // Search all joints connect to this body.
          for (jn <- b.jointList) {
            if (!jn.joint.islandFlag) {
              island.add(jn.joint)
              jn.joint.islandFlag = true

              val other = jn.other
              if ((other.flags & BodyFlags.island) == 0) {
                assert (stackCount < stackSize)
                stack(stackCount) = other
                stackCount += 1
                other.flags |= BodyFlags.island
              }
            }
          }
        }
      }

      if (!Settings.threadedIslandSolving) {
        island.solve(step, gravity, positionCorrection, allowSleep)
        positionIterationCount = max(positionIterationCount, island.positionIterationCount).toInt
      }

      // Post solve cleanup.
      for (b <- island.bodies) {
        // Allow static bodies to participate in other islands.
        if (b.isStatic) {
          b.flags &= ~BodyFlags.island
        }
      }

      if (Settings.threadedIslandSolving) {
        val taskIsland = island
        val task = new FutureTask(new Callable[Int] {
          def call = {
            taskIsland.solve(step, gravity, positionCorrection, allowSleep)
            taskIsland.positionIterationCount
          }
        })
        futures += task
        IslandSolverWorker.workQueue.put(task)
        island = new Island(bodyList.size, contactList.size, jointList.size, contactListener)
      }
      
      }
    }

    if (Settings.threadedIslandSolving) {
      for (f <- futures) {
        positionIterationCount = max(positionIterationCount, f.get).toInt
      }
    }

    //m_broadPhase.commit();

    // Synchronize shapes, check for out of range bodies.
    var iB = 0
    while (iB < bodyList.size) {
      val b = bodyList(iB)
      iB += 1
      if ((b.flags & (BodyFlags.sleep | BodyFlags.frozen)) == 0 && !b.isStatic) {
        // Update shapes (for broad-phase). If the shapes go out of
        // the world AABB then shapes and contacts may be destroyed,
        // including contacts that are
        val inRange = b.synchronizeFixtures()

        // Did the body's shapes leave the world?
        if (!inRange && boundaryListener != null) {
          boundaryListener.violation(b)
        }
      }
    }

    // Commit shape proxy movements to the broad-phase so that new contacts are created.
    // Also, some contacts can be destroyed.
    broadPhase.commit()
  }


  /** find TOI contacts and solve them. */
  private def solveTOI(step: TimeStep) {
    // Reserve an island and a stack for TOI island solution.
    val island = new Island(bodyList.size, Settings.maxTOIContactsPerIsland, 0, contactListener)
    val stackSize = bodyList.length
	val stack = new Array[Body](stackSize)

    for (b <- bodyList) {
      b.flags &= ~BodyFlags.island
      b.sweep.t0 = 0f
    }

    for (c <- contactList) {
      // Invalidate TOI
      c.flags &= ~(ContactFlags.toi | ContactFlags.island)
    }

    // Find TOI events and solve them.
    var loop = true
    while (loop) {
      // Find the first TOI.
      var minContact: Contact = null
      var minTOI = 1f

      var iCo = 0
      while (iCo < contactList.length) {
        val c = contactList(iCo)
        iCo += 1

        if ((c.flags & (ContactFlags.slow | ContactFlags.nonSolid)) == 0) {
        // simulate continue
        var skip = false
        // TODO_ERIN keep a counter on the contact, only respond to M TOIs per contact.
        var toi = 1f
        if ((c.flags & ContactFlags.toi) != 0) {
          // This contact has a valid cached TOI.
          toi = c.toi
        } else {
          // Compute the TOI for this contact.
          val f1 = c.fixture1
          val f2 = c.fixture2
          val b1 = f1.body
          val b2 = f2.body

          if ((b1.isStatic || b1.isSleeping) && (b2.isStatic || b2.isSleeping)) {
            skip = true // simulate "continue"
          } else {
            // Put the sweeps onto the same time interval.
            var t0 = b1.sweep.t0

            if (b1.sweep.t0 < b2.sweep.t0) {
              t0 = b2.sweep.t0
              b1.sweep.advance(t0)
            } else if (b2.sweep.t0 < b1.sweep.t0) {
              t0 = b1.sweep.t0
              b2.sweep.advance(t0)
            }
            assert(t0 < 1f)

            // Compute the time of impact.
            toi = c.computeTOI(b1.sweep, b2.sweep)
            assert(0f <= toi && toi <= 1f)

            if (toi > 0f && toi < 1f) {
              toi = min((1f - toi) * t0 + toi, 1f)
            }

            c.toi = toi
            c.flags |= ContactFlags.toi
          }
        }

        if (!skip) {
          if (ε < toi && toi < minTOI) {
            // This is the minimum TOI found so far.
            minContact = c
            minTOI = toi
          } 
        }
      }
      }

      if (minContact == null || 1f - 100f * ε < minTOI) {
        // No more TOI events. Done!
        loop = false
      } else {
        // Advance the bodies to the TOI.
        val f1 = minContact.fixture1
        val f2 = minContact.fixture2
        val b1 = f1.body
        val b2 = f2.body
        b1.advance(minTOI)
        b2.advance(minTOI)

        // The TOI contact likely has some new contact points.
        minContact.update(contactListener)
        minContact.flags &= ~ContactFlags.toi

        if (minContact.manifolds.isEmpty) {
          // This shouldn't happen. Numerical error?
          //b2Assert(false);
        } else {
          // Build the TOI island. We need a dynamic seed.
          val seed = if (b1.isStatic) b2 else b1

          // Reset island and stack.
          island.clear()
          var stackCount = 0
          stack(stackCount) = seed
          stackCount += 1
          seed.flags |= BodyFlags.island

          // Perform a depth first search (DFS) on the contact graph.
          while (stackCount > 0) {
            // Grab the next body off the stack and add it to the island.
            stackCount -= 1
            val b = stack(stackCount)
            island.add(b)

            // Make sure the body is awake.
            b.flags &= ~BodyFlags.sleep

            // To keep islands as small as possible, we don't
            // propagate islands across static bodies.
            if (!b.isStatic) {
              // Search all contacts connected to this body.
              for (cn <- b.contactList) {
                // Does the TOI island still have space for contacts?
                // TODO ERKKI if (island.m_contactCount < island.m_contactCapacity);
                // Has this contact already been added to an island? Skip slow or non-solid contacts.
                if ((cn.contact.flags & (ContactFlags.island | ContactFlags.slow | ContactFlags.nonSolid)) == 0 &&
                  // Is this contact touching? For performance we are not updating this contact.
                  !cn.contact.manifolds.isEmpty)
                {
                island.add(cn.contact)
                cn.contact.flags |= ContactFlags.island
                // Update other body.
                val other = cn.other

                // Was the other body already added to this island?
                if ((other.flags & BodyFlags.island) == 0) {
                  // March forward, this can do no harm since this is the min TOI.
                  if (!other.isStatic) {
                    other.advance(minTOI)
                    other.wakeUp()
                  }

                  assert(stackCount < stackSize)
                  stack(stackCount) = other
                  stackCount += 1
                  other.flags |= BodyFlags.island
                }
                }
              }
            }
          }

          val subStep = new TimeStep((1f - minTOI) * step.dt)
          assert(subStep.dt > ε)
          subStep.maxIterations = step.maxIterations

          island.solveTOI(subStep)
    		
          // Post solve cleanup.
          var iB = 0
          while (iB < island.bodies.length) {
            val b = island.bodies(iB)
            iB += 1

            b.flags &= ~BodyFlags.island
            if ((b.flags & (BodyFlags.sleep | BodyFlags.frozen)) == 0 && !b.isStatic) {
              // Allow bodies to participate in future TOI islands.

              // Update shapes (for broad-phase). If the shapes go out of
              // the world AABB then shapes and contacts may be destroyed,
              // including contacts that are
              val inRange = b.synchronizeFixtures()

              // Did the body's shapes leave the world?
              if (!inRange && boundaryListener != null) {
                boundaryListener.violation(b)
              }

              // Invalidate all contact TOIs associated with this body. Some of these
              // may not be in the island because they were not touching.
              for (cn <- b.contactList) {
                cn.contact.flags &= ~ContactFlags.toi
              }
            }
          }

          for (c <- island.contacts) {
            // Allow contacts to participate in future TOI islands.
            c.flags &= ~(ContactFlags.toi | ContactFlags.island)
          }

          // Commit shape proxy movements to the broad-phase so that new contacts are created.
          // Also, some contacts can be destroyed.
          broadPhase.commit()
        }
      }
    }
  }

}
