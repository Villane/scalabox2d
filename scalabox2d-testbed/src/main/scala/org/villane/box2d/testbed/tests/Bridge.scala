package org.villane.box2d.testbed.tests

class Bridge(_parent: TestbedMain) extends AbstractExample(_parent) {
  var firstTime = true

  val name = "Bridge"

  def create() {
    if (firstTime) {
      setCamera(0.0f,10.0f,20.0f)
      firstTime = false
    }

    scenes.Bridge.createScene
  }

}
