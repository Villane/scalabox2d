package org.villane.box2d.testbed.tests

import vecmath._
import vecmath.Preamble._
import dynamics.BodyDef
import shapes.PolygonDef
import dsl.DSL._

class Pyramid(_parent: TestbedMain) extends AbstractExample(_parent) {
  var firstTime = true

  val name = "Pyramid Stress Test"

  def create() {
    if (firstTime) {
      setCamera(2f, 12f, 10f)
      firstTime = false
    }

    body {
      pos(0, -10)
      box(50, 10)
    }

    val box1 = box(0.5f, 0.5f) density 5 restitution 0 friction 0.9f

    val sx = Vector2(-10.0f, 0.75f)
    val Δx = Vector2(0.5625f, 2.0f)
    val ex = sx + Δx * 25
    val Δy = Vector2(1.125f, 0.0f)

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
