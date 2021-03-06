package org.villane.vecmath

import Preamble._

/**
 * Extension methods for Float, mostly binary operators taking a float and a vector.
 * 
 * This class is not meant to be used explicitly.
 */
final class FloatExtensions(a: Float) {
  final def +(v: Vector2) = v + a
  final def -(v: Vector2) = Vector2(a - v.x, a - v.y)
  final def *(v: Vector2) = v * a
  final def /(v: Vector2) = Vector2(a / v.x, a / v.y)

  final def cross(v: Vector2) = Vector2(-a * v.y, a * v.x)
  final def ×(v: Vector2) = Vector2(-a * v.y, a * v.x)

  final def clamp(low: Float, high: Float) = Preamble.clamp(a, low, high)

  final def isValid = a != Float.NaN && a != Float.NegativeInfinity && a != Float.PositiveInfinity 
}
