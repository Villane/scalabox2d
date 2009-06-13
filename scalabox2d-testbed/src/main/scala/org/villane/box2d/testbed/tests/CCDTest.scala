package org.villane.box2d.testbed.tests

import vecmath._
import vecmath.Preamble._
import dynamics.joints._
import dynamics._
import shapes._
import dsl.DSL._

class CCDTest(_parent: TestbedMain) extends AbstractExample(_parent) {
  var firstTime = true
  val name = "Continuous Collision Test"

  def create() {
    if (firstTime) {
      setCamera(0f, 20f, 20f)
      firstTime = false
    }

    val k_restitution = 1.4f

    body {
      pos(0, 20)
      box(0.1f, 10.0f, (-10.0f, 0.0f), 0.0f) density 0 restitution k_restitution
      box(0.1f, 10.0f, (10.0f, 0.0f), 0.0f) density 0 restitution k_restitution
      box(0.1f, 10.0f, (0.0f, -10.0f), 0.5f * π) density 0 restitution k_restitution
      box(0.1f, 10.0f, (0.0f, 10.0f), -0.5f * π) density 0 restitution k_restitution
    }

    body {
      pos(0.0f, 15.0f)
      // bottom
      box(1.5f, 0.15f) density 4.0f
      // left
      box(0.15f, 2.7f, (-1.45f, 2.35f), 0.2f) density 4.0f
      // right
      box(0.15f, 2.7f, (1.45f, 2.35f), -0.2f) density 4.0f
      bullet(true)
      massFromShapes
    }

    for (i <- 0 until 10) body {
      pos(0.0f, 15.5f + i)
      circle(0.25f) density 1.0f restitution 0.0f friction 0.05f
      bullet(true)
      massFromShapes
    } angularVelocity = new util.Random().nextFloat * 100 - 50
  }
}
