package org.villane.box2d.testbed.tests

import vecmath._
import vecmath.Preamble._
import dynamics.joints._
import dynamics._
import shapes._
import dsl.DSL._

class Circles(_parent: TestbedMain) extends AbstractExample(_parent) {
  var firstTime = true

  val name = "Circle Stress Test"

  def create() {
    if (firstTime) {
      setCamera(0f, 20f, 5f)
      firstTime = false
    }

    val ground = m_world.groundBody
    body {
      // Ground
      pos(0.0f, -10.0f)
      box(50.0f, 10.0f) friction 1.0f
    }
    
    val leftWall = body {
      pos(53.0f, 25.0f)
      box(3.0f,50.0f) friction 1.0f
    }
    val rightWall = body {
      pos(-53.0f, 25.0f)
      box(3.0f,50.0f) friction 1.0f
    }
    // Corners 
    body {
      pos(-40f, 0.0f)
      angle(-π/4)
      box(20.0f, 3.0f) friction 1.0f
    }
    body {
      pos(40f, 0.0f)
      angle(π/4)
      box(20.0f, 3.0f) friction 1.0f
    }

    val bd = body {
      pos(0.0f, 10.0f)
      val numPieces = 5
      val radius = 6f
      for (i <- 0 until numPieces) {
        val x = radius * cos(2 * π * (i.toFloat / (numPieces)))
        val y = radius * sin(2 * π * (i.toFloat / (numPieces)))
        circle((x, y), 1.2f) density 25.0f friction 0.1f restitution 0.9f
      }
      massFromShapes
    }

    joint (
      revolute(bd -> ground)
        anchor bd.pos
        motorSpeed π
        maxMotorTorque 1000000.0f
        enableMotor true
    )

    val loadSize = 45
    for (j <- 0 until 10) {
      for (i <- 0 until loadSize) body {
        pos(-45f + 2*i, 50f + j)
        (circle(1.0f+(if (i%2==0) 1.0f else -1.0f)*.5f*(i.toFloat/loadSize))
          density 5.0f
          friction 0.1f
          restitution 0.5f
        )
        massFromShapes
      }
    }
  }

}