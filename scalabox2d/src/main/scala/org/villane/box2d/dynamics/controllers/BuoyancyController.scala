package org.villane.box2d.dynamics.controllers

import vecmath._
import vecmath.Preamble._

/** Calculates buoyancy forces for fluids in the form of a half plane. */
class BuoyancyController extends Controller {
  /** The outer surface normal */
  var normal = Vector2.Zero
  /** The height of the fluid surface along the normal */
  var offset = 0f
  /** The fluid density */
  var density = 0f
  /** Fluid velocity, for drag calculations */
  var velocity = Vector2.Zero
  /** Linear drag co-efficient */
  var linearDrag = 0f
  /** Angular drag co-efficient */
  var angularDrag = 0f
  /**
   * If false, bodies are assumed to be uniformly dense, otherwise use the shapes densities.
   * False by default to prevent a gotcha.
   */
  var useDensity = false
  /** If true, gravity is taken from the world instead of the gravity parameter. */
  var useWorldGravity = false
  /** Gravity vector, if the world's gravity is not used */
  var gravity = Vector2.Zero

  def step(step: TimeStep) {
    //Buoyancy force is just a function of position,
    //so unlike most forces, it is safe to ignore sleeping bodes
    forAwakeBodies { body =>
      var areac = Vector2.Zero
      var massc = Vector2.Zero
      var area = 0f
      var mass = 0f
      for (f <- body.fixtures) {
        val (sarea, sc) = f.computeSubmergedArea(normal, offset)
        area += sarea
        areac += sarea * sc
        val shapeDensity = if (useDensity) f.density else 1f
        mass += sarea * shapeDensity
        massc += sarea * sc * shapeDensity
      }
      areac /= area

      val localCentroid = body.transform ** areac
      massc /= mass
      if (area >= Settings.Epsilon) {
        //Buoyancy
        val buoyancyForce = -density * area * gravity
        body.applyForce(buoyancyForce, massc)
        //Linear drag
        var dragForce = body.getLinearVelocityFromWorldPoint(areac) - velocity
        dragForce *= -linearDrag * area
        body.applyForce(dragForce, areac)
        //Angular drag
        //TODO: Something that makes more physical sense?
        body.applyTorque(-body.I / body.mass * area * body.angularVelocity * angularDrag)
      }
    }
  }

}
