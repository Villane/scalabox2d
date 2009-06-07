package org.villane.vecmath

object MathUtil {
  val π = Math.Pi.toFloat
  val Pi = π

  // Max/min rewritten here because for some reason Math.max/min
  // can run absurdly slow for such simple functions...
  // TODO: profile, see if this just seems to be the case or is actually causing issues...
  def max(a: Float, b: Float) = if (a > b) a else b
  def max(as: Float*): Float = as reduceLeft max
  def min(a: Float, b: Float) = if (a < b) a else b
  def min(as: Float*): Float = as reduceLeft min

  def sqrt(a: Float) = Math.sqrt(a).toFloat

  def sin(a: Float) = Math.sin(a).toFloat
  def cos(a: Float) = Math.cos(a).toFloat
  def tan(a: Float) = Math.tan(a).toFloat
  def asin(a: Float) = Math.asin(a).toFloat
  def acos(a: Float) = Math.acos(a).toFloat
  def atan(a: Float) = Math.atan(a).toFloat
  def pow(a: Float, b: Float) = Math.pow(a, b).toFloat

  def clamp(a: Float, low: Float, high: Float) = max(low, min(a, high))

  def distance(a: Vector2f, b: Vector2f) = (a - b).length

  def distanceSquared(a: Vector2f, b: Vector2f) = {
    val d = a - b
    d dot d
  }

}
