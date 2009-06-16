package org.villane.box2d.testbed.scenes

import vecmath._
import vecmath.Preamble._
import dsl.DSL._

object Chain extends TestbedScene {

  def createScene(implicit world: dynamics.World) {
    val ground = body {
      pos(0.0f, -10.0f)
      box(50.0f, 10.0f)
    }

    val sd = box(0.6f, 0.125f) density 20.0f friction 0.2f
    body {
      val y = 25.0f
      var prevBody = ground
      for (i <- 0 until 30) {
        val bd = body {
          pos(0.5f + i, y)
          fixture(sd)
          massFromShapes
        }

        joint (
          revolute(prevBody -> bd)
            anchor(i, y)
            collideConnected(false)
        )
        prevBody = bd
      }
    }
  }

}
