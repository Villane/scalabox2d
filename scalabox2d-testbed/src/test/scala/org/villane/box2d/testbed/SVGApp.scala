package org.villane.box2d.testbed

import svg._
import draw.DrawFlags._

object SVGApp {
  val scale = 15

  def main(args: Array[String]) {
    val loader = new SlickSVGSceneLoader("C:/drawing.svg", scale)
    val world = loader.create
    println("Done!")
    val flags = Shapes
    slick.SlickDisplayWorld.runWithSimulation(world, false, flags)
  }

}
