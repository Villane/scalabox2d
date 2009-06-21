package org.villane.box2d.testbed

object CirclesPerfTest {

  def main(args: Array[String]) {
    val test = new HeadlessPerformanceTest(scenes.Circles)
    test.run(true, 300, 150, true)
  }

}
