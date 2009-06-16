package org.villane.box2d.testbed

object PyramidPerfTest {

  def main(args: Array[String]) {
    val test = new HeadlessPerformanceTest(scenes.Pyramid)
    test.run(true, 1000, 100, false)
  }

}
