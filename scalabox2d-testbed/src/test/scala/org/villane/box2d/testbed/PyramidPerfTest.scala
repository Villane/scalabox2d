package org.villane.box2d.testbed

object PyramidPerfTest {

  def main(args: Array[String]) {
    val test = new HeadlessPerformanceTest(scenes.Pyramid)
    test.run(true, 300, 50, true)
    //test.run(true, 50, 10, true)
  }

}
