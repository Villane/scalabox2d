package org.villane.box2d.testbed

import draw._

trait TestbedMain {
  /** Drawing handler to use. */
  def dd: DebugDraw
  def mouseX: Float
  def mouseY: Float
  def shiftKey: Boolean
  def height: Int
  def width: Int
}
