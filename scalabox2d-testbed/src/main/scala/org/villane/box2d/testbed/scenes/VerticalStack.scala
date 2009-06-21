package org.villane.box2d.testbed.scenes

import vecmath._
import vecmath.Preamble._
import dsl.DSL._

import scala.util.Random

object VerticalStack extends TestbedScene {

  def createScene(implicit world: dynamics.World) {
    body {
      pos(0.0f, 0.0f)
      box(50.0f, 10.0f, (0.0f, -10.0f), 0.0f)
      box(0.1f, 10.0f, (20.0f, 10.0f), 0.0f)
    }

    val xs = Array(0.0f, -10.0f, -5.0f, 5.0f, 10.0f)
    val sd = box(0.5f, 0.5f) density 1.0f friction 0.3f

    for {
      j <- 0 until xs.length
      i <- 0 until 12
    } body {
      //float32 x = b2Random(-0.1f, 0.1f);
      //float32 x = i % 2 == 0 ? -0.025f : 0.025f;
      //bd.position.Set(xs[j], 2.51f + 4.02f * i);
      pos(xs(j) + (new Random().nextFloat * 0.1f - 0.05f),
          0.752f + 1.54f * i)
      fixture(sd)
      // For this test we are using continuous physics for all boxes.
      // This is a stress test, you normally wouldn't do this for
      // performance reasons.
      bullet(true)
      sleepingAllowed(true)
      massFromShapes
    }
  }

}
