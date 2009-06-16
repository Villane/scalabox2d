package org.villane.box2d.testbed.scenes

import vecmath._
import vecmath.Preamble._
import dsl.DSL._

object Domino extends TestbedScene {

  def createScene(implicit world: dynamics.World) {
    // Floor
    body {
      pos(0, -10)
      box(50.0f, 10.0f)
    }

    // Platforms
    for (i <- 0 to 3) body {
      pos(0.0f, 5f + 5f * i)
      box(15.0f, 0.125f)
    }

    val sd = box(0.125f, 2f) density 25.0f friction 0.5f
    var numPerRow = 25

    for (i <- 0 to 3) {
      for (j <- 0 until numPerRow) body {
        var p = Vector2(-14.75f + j * (29.5f / (numPerRow - 1)),
                         7.3f + 5f * i)
        if (i == 2 && j == 0) {
          angle(-0.1f)
          p += (0.1f, 0)
        } else if (i == 3 && j == numPerRow - 1) {
          angle(0.1f)
          p -= (0.1f, 0)
        } else {
          angle(0f)
        }
        pos(p)
        fixture(sd)
        massFromShapes
      }
    }
  }

}
