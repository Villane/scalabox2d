package org.villane.box2d.testbed.tests

import vecmath._
import vecmath.Preamble._
import dynamics.joints._
import dynamics._
import shapes._
import dsl.DSL._

class Bridge(_parent: TestbedMain) extends AbstractExample(_parent) {
  var firstTime = true

  val name = "Bridge"

  def create() {
    if (firstTime) {
      setCamera(0.0f,10.0f,20.0f)
      firstTime = false
    }

    val ground = body {
      pos(0,0)
      box(50.0f, 0.2f)
    }

    val sd = box(0.65f, 0.125f) density 20.0f friction 0.2f

    val numPlanks = 30

    var prevBody = ground
    for (i <- 0 until numPlanks) {
      val bd = body {
        pos(-14.5f + 1.0f * i, 5.0f)
        fixture(sd)
        massFromShapes
      }
      joint(revolute(prevBody -> bd) anchor(-15.0f + 1.0f * i, 5.0f))
      prevBody = bd
	}

    joint(revolute(prevBody -> ground) anchor(-15.0f + 1.0f * numPlanks, 5.0f))

    body {
      pos(0, 10)
      box(1,1) density 5 friction 0.2f restitution 0.1f
      massFromShapes
    }

    body {
      pos(0, 12)
      circle((0, 0), 0.9f) density 5 friction 0.2f
      circle((0, 1), 0.9f) density 5 friction 0.2f
      massFromShapes
    }
  }

}
