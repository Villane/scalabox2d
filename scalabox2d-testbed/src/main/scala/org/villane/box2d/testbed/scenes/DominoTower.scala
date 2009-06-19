package org.villane.box2d.testbed.scenes

import vecmath._
import vecmath.Preamble._
import dsl.DSL._
import dynamics.World

// ERKKI: this was 120 Hz in JBox2D
object DominoTower extends TestbedScene {
  val dwidth = 0.2f
  val dheight = 1f
  var ddensity = 10f
  val dfriction = 0.1f
  val baseCount = 25

  def makeDomino(x: Float, y: Float, horizontal: Boolean)(implicit world: World) =
    body {
      pos(x, y)
      angle(if (horizontal) Pi / 2 else 0f)
      (box(dwidth / 2, dheight / 2)
         density ddensity
         friction dfriction
         restitution 0.65f)
      massFromShapes
    }

  def createScene(implicit world: dynamics.World) {
    body { // Floor
      pos(0, -10)
      box(50, 10)
    }
    
    //Make bullets
    val bs = box(0.7f, 0.7f) density 35f friction 0f restitution 0.85f
    var b = body {
      bullet(true)
      pos(30, 50)
      fixture(bs)
      massFromShapes
    }
    b.linearVelocity = (-25, -25)
    b.angularVelocity = 6.7f
    b = body {
      pos(-30, 25)
      fixture(bs) density 25f
      massFromShapes
    }
    b.linearVelocity = (35, 10)
    b.angularVelocity = -8.3f

    //Make base
    for (i <- 0 until baseCount) {
      val currX = i * 1.5f * dheight - (1.5f * dheight * baseCount / 2f)
      makeDomino(currX, dheight / 2.0f, false)
      makeDomino(currX, dheight + dwidth / 2.0f, true)
    }

    //Make 'I's
    for (j <- 1 until baseCount) {
      if (j > 3) ddensity *= .8f
      //y at center of 'I' structure
      val currY = dheight * 0.5f + (dheight + 2f * dwidth) * 0.99f * j

      for (i <- 0 until baseCount - j) {
        // + random(-.05f, .05f);
        val currX = i * 1.5f * dheight - (1.5f * dheight * (baseCount - j) /2f)
        ddensity *= 2.5f
        if (i == 0)
          makeDomino(currX - (1.25f * dheight) + 0.5f * dwidth, currY - dwidth, false)
        if ((i == baseCount - j - 1) && (j != 1))
          makeDomino(currX + (1.25f * dheight) - 0.5f * dwidth, currY - dwidth, false)
        ddensity /= 2.5f
        makeDomino(currX, currY, false)
        makeDomino(currX, currY + 0.5f * (dwidth + dheight), true)
        makeDomino(currX, currY - 0.5f * (dwidth + dheight), true)
      }
    }
  }

}
