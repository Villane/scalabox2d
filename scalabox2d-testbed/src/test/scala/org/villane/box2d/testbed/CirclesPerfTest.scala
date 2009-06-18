package org.villane.box2d.testbed

object CirclesPerfTest {

  def main(args: Array[String]) {
    val test = new HeadlessPerformanceTest(scenes.Circles)
    test.run(true, 100, 0, false)
  }

}
