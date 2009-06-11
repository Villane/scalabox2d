package org.villane.box2d.testbed.tests

import vecmath._
import vecmath.Preamble._
import dynamics.joints._
import dynamics._
import shapes._
import dsl.DSL._

class VaryingRestitution(_parent: TestbedMain) extends AbstractExample(_parent) {

  val name = "Varying Restitution"

  def create() {
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
