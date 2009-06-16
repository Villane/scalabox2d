package org.villane.box2d.testbed.tests

class Circles(_parent: TestbedMain) extends AbstractExample(_parent) {
  var firstTime = true

  val name = "Circle Stress Test"

  def create() {
    if (firstTime) {
      setCamera(0f, 20f, 5f)
      firstTime = false
    }

    scenes.Circles.createScene
  }

}