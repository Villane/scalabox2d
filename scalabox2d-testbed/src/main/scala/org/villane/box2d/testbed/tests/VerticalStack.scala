package org.villane.box2d.testbed.tests

import vecmath._
import vecmath.Preamble._

class VerticalStack(_parent: TestbedMain) extends AbstractExample(_parent) {
  var firstTime = true
  val name = "Vertical Stack"
  override val getExampleInstructions = "Press , to shoot sideways bullet\n"

  override def postStep() {
    if (newKeyDown(0x33)) {
      val rnd = new scala.util.Random
      launchBomb((-20.0f, 1f + rnd.nextFloat * 9),
                 (750.0f, rnd.nextFloat * 10 - 5))
    }
  }

  def create = {
    if (firstTime) {
      setCamera(0f, 10f, 15f)
      firstTime = false
    }

    scenes.VerticalStack.create
  }

}
