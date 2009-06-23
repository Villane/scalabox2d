package org.villane.box2d.draw

// make sure we use actual Float even if Float is replaced with Double 
import scala.Float

/** TODO rename to Color4 ?*/
object Color3f {
  def apply(r: Float, g: Float, b: Float): Color3f = Color3f(r, g, b, 255)
  /** Create a color specifying RGB components from 0.0f to 1.0f */
  def ratio(r: Float, g: Float, b: Float) = Color3f(255 * r, 255 * g, 255 * b)
  def ratio(r: Float, g: Float, b: Float, a: Float) = Color3f(255 * r, 255 * g, 255 * b, 255 * a)
}

/**
 * Color of RGB components where white is (255,255,255) and black is (0,0,0)
 */
case class Color3f(r: Float, g: Float, b: Float, a: Float)
