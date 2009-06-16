package org.villane.box2d.testbed.scenes

import vecmath._
import vecmath.Preamble._
import dsl.DSL._

object VaryingRestitution extends TestbedScene {

  def createScene(implicit world: dynamics.World) {
    body {
      pos(0.0f, -10.0f)
      box(50.0f, 10.0f)
    }

    val sd = circle(0.6f) density 5.0f
    val restitution = Array(0.0f, 0.1f, 0.3f, 0.5f, 0.75f, 0.9f, 1.0f)

    for (i <- 0 until restitution.length) body {
      pos(-10.0f + 3.0f * i, 10.0f)
      fixture(sd) restitution restitution(i)
      massFromShapes
    }
  }

}
