package org.villane.box2d.testbed.scenes

import vecmath._
import vecmath.Preamble._
import dsl.DSL._

object Pyramid extends TestbedScene {

  def createScene(implicit world: dynamics.World) {
    body {
      pos(0, -10)
      box(50, 10)
    }

    val box1 = box(0.5f, 0.5f) density 5 restitution 0 friction 0.9f

    val sx = (-10.0f, 0.75f)
    val Δx = (0.5625f, 2.0f)
    val ex = sx + Δx * 25
    val Δy = (1.125f, 0.0f)

    for {
      x <- sx to ex by Δx
      y <- x to (x + Δy * (ex.y - x.y) / Δx.y) by Δy
    } body {
      pos(y)
      fixture(box1)
      massFromShapes
    }
  }

}
