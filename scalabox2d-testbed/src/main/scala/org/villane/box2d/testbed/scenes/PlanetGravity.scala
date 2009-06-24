package org.villane.box2d.testbed.scenes

import vecmath._
import vecmath.Preamble._
import shapes.AABB
import dsl.DSL._
import dynamics._
import controllers._

object PlanetGravity extends TestbedScene {

  var planet: Body = null
  var player: Body = null

  def createScene(implicit world: dynamics.World) {
    planet = body {
      circle(5)
    }

    player = body {
      pos(0f, 7f)
      box(0.4f, 1.2f) density 1
      massFromShapes
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
    (0, 0),
    true
  )

}
