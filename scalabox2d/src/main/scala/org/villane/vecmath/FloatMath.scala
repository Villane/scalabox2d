package org.villane.vecmath

/**
 * A trait implementing math functions with Float signatures.
 * 
 * Replacable with DoubleMath.
 * 
 * TODO: for Scala 2.8, make FloatMath and DoubleMath extend
 * MathFunctions[@specialized T] to enforce a common interface.
 */
trait FloatMath {
  final val Pi = Math.Pi.toFloat
  final val π = Pi

  final val Infinity = Float.PositiveInfinity
  final val ∞ = Infinity

  // Max/min rewritten here because for some reason Math.max/min
  // can run absurdly slow for such simple functions...
  // TODO: profile, see if this just seems to be the case or is actually causing issues...
  final def max(a: Float, b: Float) = if (a > b) a else b
  final def max(as: Float*): Float = as reduceLeft max
  final def min(a: Float, b: Float) = if (a < b) a else b
  final def min(as: Float*): Float = as reduceLeft min
  final def clamp(a: Float, low: Float, high: Float) =
    if (a < low) low
    else if (a > high) high
    else a

  final def sqrt(a: Float) = Math.sqrt(a).toFloat
  final def √(a: Float) = sqrt(a)

  final def sin(a: Float) = Math.sin(a).toFloat
  final def cos(a: Float) = Math.cos(a).toFloat
  final def tan(a: Float) = Math.tan(a).toFloat
  final def asin(a: Float) = Math.asin(a).toFloat
  final def acos(a: Float) = Math.acos(a).toFloat
  final def atan(a: Float) = Math.atan(a).toFloat
  final def atan2(y: Float, x: Float) = Math.atan2(y, x).toFloat

  final def pow(a: Float, b: Float) = Math.pow(a, b).toFloat
}
