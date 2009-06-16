package org.villane.box2d.testbed.tests

class Chain(_parent: TestbedMain) extends AbstractExample(_parent) {
  var firstTime = true
  
  val name = "Chain"

  def create() {
    if (firstTime) {
      setCamera(0.0f,10.0f,10.0f);
      firstTime = false;
    }

    scenes.Chain.createScene
  }
}
