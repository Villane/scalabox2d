package org.villane.vecmath

import Preamble._

/**
 * Extension methods for Scalar, mostly binary operators taking a float and a vector.
 * 
 * This class is not meant to be used explicitly.
 */
final class ScalarExtensions(a: Scalar) {
  final def +(v: Vector2) = v + a
  final def -(v: Vector2) = Vector2(a - v.x, a - v.y)
  final def *(v: Vector2) = v * a
  final def /(v: Vector2) = Vector2(a / v.x, a / v.y)

  final def cross(v: Vector2) = Vector2(-a * v.y, a * v.x)
  final def ×(v: Vector2) = Vector2(-a * v.y, a * v.x)

  final def clamp(low: Scalar, high: Scalar) = Preamble.clamp(a, low, high)

  final def isValid = a != Scalar.NaN && a != Scalar.NegativeInfinity && a != Scalar.PositiveInfinity 
}
