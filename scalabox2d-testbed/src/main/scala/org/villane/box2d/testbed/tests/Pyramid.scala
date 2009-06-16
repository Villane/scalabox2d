package org.villane.box2d.testbed.tests

class Pyramid(_parent: TestbedMain) extends AbstractExample(_parent) {
  var firstTime = true

  val name = "Pyramid Stress Test"

  def create() {
    if (firstTime) {
      setCamera(2f, 12f, 10f)
      firstTime = false
    }

    scenes.Pyramid.createScene
  }

}
