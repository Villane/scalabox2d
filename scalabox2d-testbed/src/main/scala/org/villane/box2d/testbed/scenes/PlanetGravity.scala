package org.villane.box2d.testbed.scenes

import vecmath._
import vecmath.Preamble._
import shapes.AABB
import dsl.DSL._
import dynamics._
import controllers._

object PlanetGravity extends TestbedScene {

  def createScene(implicit world: dynamics.World) {
    val planet = body {
      circle(5)
    }

    val satellite = body {
      pos(10, 10)
      circle(0.5f) density 2
      massFromShapes
    }
    satellite.applyForce((150f, -150f), satellite.worldCenter)
    satellite.angularVelocity = -0.4f

    val gc = new PlanetGravityController
    gc.createSensor(planet, 15)
    gc.G = 9.81f
    world.addController(gc)
  }

  override protected def createWorld = new World(
    AABB((-200, -100), (200, 200)),
    (0, 10),
    true
  )

}
