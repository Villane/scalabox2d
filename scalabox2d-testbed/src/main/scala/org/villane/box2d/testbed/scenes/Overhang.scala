package org.villane.box2d.testbed.scenes

import vecmath._
import vecmath.Preamble._
import dsl.DSL._

object Overhang extends TestbedScene {

  def createScene(implicit world: dynamics.World) {
    body {
      pos(0.0f, -10.0f)
      box(50.0f, 10.0f)
    }

    val w = 4.0f
    val h = 0.25f
    val sd = box(w, h) density 1 friction 0.3f restitution 0

    val numSlats = 8
    val eps = 0.14f
    var lastCMX = 0.0f
    for (i <- 0 until numSlats) body {
      val newX = lastCMX + w - eps
      lastCMX = (i * lastCMX + newX) / (i + 1)
      pos(newX, 0.25f + 2 * h * (numSlats - i - 1))
      fixture(sd)
      massFromShapes
    }
  }

}
