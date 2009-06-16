package org.villane.box2d.testbed.tests

class VerticalStack(_parent: TestbedMain) extends AbstractExample(_parent) {
  var firstTime = true
  val name = "Vertical Stack"
  override val getExampleInstructions = "Press , to shoot sideways bullet\n"

  override def postStep() {
    /*if (newKeyDown[',']) {
      launchBomb(new Vec2(-40.0f,parent.random(1.0f,10.0f)),new Vec2(200.0f,parent.random(-5.0f,5.0f)));
    }*/
  }


  def create() {
    if (firstTime) {
      setCamera(0f, 10f, 10f)
      firstTime = false
    }

    scenes.VerticalStack.createScene
  }

}
