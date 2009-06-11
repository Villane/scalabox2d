package org.villane.box2d.testbed.tests

import vecmath._
import vecmath.Preamble._
import dynamics.joints._
import dynamics._
import shapes._
import dsl.DSL._

class VaryingFriction(_parent: TestbedMain) extends AbstractExample(_parent) {
  val name = "Varying Friction"

  def create() {
    body {
      pos(0.0f, -20.0f)
      box(100.0f, 20.0f)
    }

    body {
      pos(-4.0f, 22.0f)
      angle(-0.25f)
      box(13.0f, 0.25f)
    }

    body {
      pos(10.5f, 19.0f)
      box(0.25f, 1.0f)
    }

    body {
      pos(4.0f, 14.0f)
      angle(0.25f)
      box(13.0f, 0.25f)
    }

    body {
      pos(-10.5f, 11.0f)
      box(0.25f, 1.0f)
    }

    body {
      pos(-4.0f, 6.0f)
      angle(-0.25f)
      box(13.0f, 0.25f)
    }

    val sd = box(0.5f, 0.5f) density 25.0f
    val friction = Array(0.75f, 0.5f, 0.35f, 0.1f, 0.0f)

    for (i <- 0 until 5) body {
      pos(-15.0f + 4.0f * i, 28.0f)
      fixture(sd) friction friction(i)
      massFromShapes
    }
  }

}
