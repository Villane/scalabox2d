package org.villane.vecmath

object MathUtil {
  val π = Math.Pi.toFloat
  val Pi = π

  // Max/min rewritten here because for some reason Math.max/min
  // can run absurdly slow for such simple functions...
  // TODO: profile, see if this just seems to be the case or is actually causing issues...
  def max(a: Float, b: Float) = if (a > b) a else b
  def min(a: Float, b: Float) = if (a < b) a else b

  def sqrt(a: Float) = Math.sqrt(a).toFloat

  def sin(a: Float) = Math.sin(a).toFloat
  def cos(a: Float) = Math.cos(a).toFloat

  def clamp(a: Float, low: Float, high: Float) = max(low, min(a, high))
}
