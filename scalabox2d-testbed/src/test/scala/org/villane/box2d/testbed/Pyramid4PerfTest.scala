package org.villane.box2d.testbed

import vecmath._
import vecmath.Preamble._
import dsl.DSL._

object Pyramid4PerfTest extends scenes.TestbedScene {

  def main(args: Array[String]) {
    val test = new HeadlessPerformanceTest(this)
    test.run(true, 1000, 100, false)
  }

  def createScene(implicit world: dynamics.World) {
    body {
      pos(0, -10)
      box(50, 10)
    }

    val box1 = box(0.5f, 0.5f) density 5f restitution 0f friction 0.9f
	var x = Vector2(-30.0f, 0.75f);
	var y = Vector2.Zero
	val deltaX = Vector2(0.5625f, 2.0f);
	val deltaY = Vector2(1.125f, 0.0f);

    val num = 17
    def loop = for (i <- 0 until num) {
		y = x
		for (j <- i until num) body {
			pos(y)
			fixture(box1)
			massFromShapes

			y += deltaY
		}
		x += deltaX
	}
    loop
    x = (-10f, 0.75f)
    loop
    x = (10f, 0.75f)
    loop
    x = (30f, 0.75f)
    loop
  }
	
}
