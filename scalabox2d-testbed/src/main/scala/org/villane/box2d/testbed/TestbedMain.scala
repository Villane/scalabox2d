package org.villane.box2d.testbed

import vecmath.Preamble._
import draw._

trait TestbedMain {
  /** Drawing handler to use. */
  def dd: DebugDraw
  def mouseX: Scalar
  def mouseY: Scalar
  def shiftKey: Boolean
  def height: Int
  def width: Int
}
