package org.villane.vecmath

/**
 * Mirrors FloatMath, use when replacing Float with Double
 */
trait DoubleMath {
  final val Pi = Math.Pi
  final val π = Math.Pi

  // Max/min rewritten here because for some reason Math.max/min
  // can run absurdly slow for such simple functions...
  // TODO: profile, see if this just seems to be the case or is actually causing issues...
  final def max(a: Double, b: Double) = if (a > b) a else b
  final def max(as: Double*): Double = as reduceLeft max
  final def min(a: Double, b: Double) = if (a < b) a else b
  final def min(as: Double*): Double = as reduceLeft min
  final def clamp(a: Double, low: Double, high: Double) = max(low, min(a, high))

  final def sqrt(a: Double) = Math.sqrt(a)

  final def sin(a: Double) = Math.sin(a)
  final def cos(a: Double) = Math.cos(a)
  final def tan(a: Double) = Math.tan(a)
  final def asin(a: Double) = Math.asin(a)
  final def acos(a: Double) = Math.acos(a)
  final def atan(a: Double) = Math.atan(a)
  final def atan2(a: Double, b: Double) = Math.atan2(a, b)

  final def pow(a: Double, b: Double) = Math.pow(a, b)

  final def distance(a: Vector2, b: Vector2) = (a - b).length

  final def distanceSquared(a: Vector2, b: Vector2) = {
    val d = a - b
    d dot d
  }
}
