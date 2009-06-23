package org.villane.box2d.testbed.tests

import vecmath._
import scenes._

object SimpleTest {
  def apply(parent: TestbedMain,
            name: String,
            sf: TestbedScene,
            offset: Vector2, scale: Float) =
    new SimpleTest(parent, name, sf, offset, scale)

  def apply(parent: TestbedMain, name: String, sf: TestbedScene, scale: Float) =
    new SimpleTest(parent, name, sf, Vector2.Zero, scale)
}

/**
 * Simple test without custom instructions or custom key mapping.
 * Can be created given a scene.
 */
class SimpleTest(
  _parent: TestbedMain,
  val name: String,
  scene: TestbedScene,
  offset: Vector2,
  scale: Float
) extends AbstractExample(_parent) {
  var firstTime = true

  def create = {
    if (firstTime) {
      setCamera(offset.x, offset.y, scale)
      firstTime = false
    }

    scene.create
  }

}
