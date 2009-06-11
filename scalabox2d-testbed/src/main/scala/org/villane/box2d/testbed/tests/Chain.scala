package org.villane.box2d.testbed.tests

import vecmath._
import vecmath.Preamble._
import dynamics.joints._
import dynamics._
import shapes._
import dsl.DSL._

class Chain(_parent: TestbedMain) extends AbstractExample(_parent) {
  var firstTime = true
  
  val name = "Chain"

  def create() {
    if (firstTime) {
      setCamera(0.0f,10.0f,10.0f);
      firstTime = false;
    }

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
