package org.villane.box2d.testbed.tests

class CCDTest(_parent: TestbedMain) extends AbstractExample(_parent) {
  var firstTime = true
  val name = "Continuous Collision Test"

  def create() {
    if (firstTime) {
      setCamera(0f, 20f, 20f)
      firstTime = false
    }

    scenes.CCDTest.createScene
  }
}
