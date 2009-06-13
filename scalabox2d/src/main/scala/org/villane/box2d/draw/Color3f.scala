package org.villane.box2d.draw

// make sure we use actual Float even if Float is replaced with Double 
import scala.Float

object Color3f {
  /**
   * Create a color specifying RGB components from 0.0f to 1.0f
   */
  def ratio(r: Float, g: Float, b: Float) = Color3f(255 * r, 255 * g, 255 * b)
}

/**
 * Color of RGB components where white is (255,255,255) and black is (0,0,0)
 */
case class Color3f(r: Float, g: Float, b: Float)
