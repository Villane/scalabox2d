package org.villane.vecmath

/**
 * Extension methods for Float, mostly binary operators taking a float and a vector.
 * 
 * This class is not meant to be used explicitly.
 */
class FloatExtensions(a: Float) {
  def +(v: Vector2f) = v + a
  def -(v: Vector2f) = Vector2f(a - v.x, a - v.y)
  def *(v: Vector2f) = v * a
  def /(v: Vector2f) = Vector2f(a / v.x, a / v.y)

  def cross(v: Vector2f) = Vector2f(-a * v.y, a * v.x)
  def ×(v: Vector2f) = Vector2f(-a * v.y, a * v.x)

  def clamp(low: Float, high: Float) = MathUtil.clamp(a, low, high)

  def isValid = a != Float.NaN && a != Float.NegativeInfinity && a != Float.PositiveInfinity 
}
