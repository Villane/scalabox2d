package org.villane.box2d.testbed.scenes

import vecmath._
import vecmath.Preamble._
import dsl.DSL._

object Pistons extends TestbedScene {
  val Bullets = false

  def createScene(implicit world: dynamics.World) {
    var prevBody = world.groundBody

    // Define crank.
    var bd = body {
      pos(0.0f, 7.0f)
      box(0.5f, 2.0f) density 1.0f
      massFromShapes
    }

    val joint1 = joint(
      revolute(prevBody -> bd)
        anchor(0.0f, 5.0f)
        motorSpeed 1.0f * 3.1415f
        maxMotorTorque Float.MaxValue
        enableMotor true
    )

    prevBody = bd

    // Define follower.
    bd = body {
      pos(0.0f, 13.0f)
      box(0.5f, 4.0f) density 1.0f
      massFromShapes
    }

    joint(
      revolute(prevBody -> bd)
        anchor(0.0f, 9.0f)
        enableMotor false
    )

    prevBody = bd

    // Define piston
    bd = body {
      pos(0.0f, 17.0f)
      box(5.0f, 1.5f) density 1.0f
      massFromShapes
    }

    joint(revolute(prevBody -> bd) anchor(0.0f, 17.0f))

    val joint2 = joint(
      prismatic(world.groundBody -> bd)
        anchor(0.0f, 17.0f)
        axis(0.0f, 1.0f)
        //maxMotorForce Float.MaxValue
        enableMotor false
    )

    // Create a payload
    for (i <- 0 until 100) body {
      pos(-1.0f, 23.0f + i)
      box(0.4f,0.3f) density 0.1f
      bullet(Bullets)
      massFromShapes
    }

    for (i <- 0 until 100) body {
      pos(1.0f, 23.0f + i)
      circle(0.36f) density 2.0f
      bullet(Bullets)
      massFromShapes
    }

    body {
      pos(6.1f, 50.0f)
      box(1.0f, 100.0f) density 0.0f friction 0.0f
    }
    body {
      pos(-6.1f, 50.0f)
      box(1.0f, 100.0f) density 0.0f friction 0.0f
    }
  }

}
