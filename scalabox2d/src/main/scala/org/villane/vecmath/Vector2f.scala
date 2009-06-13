package org.villane.vecmath

import Preamble._

/**
 * Vector2f creation methods and often used constant values.
 */
object Vector2f {
  val Zero = new Vector2f(0, 0)
  val One = Vector2f(1, 1)
  val XUnit = Vector2f(1, 0)
  val YUnit = Vector2f(0, 1)
  def polar(r: Float, theta: Float) = Vector2f(cos(theta) * r, sin(theta) * r)

  // for debugging only
  var creationCount = 0L
}

/**
 * An immutable 2D Vector represented as x and y coordinates in single precision
 * floating point numbers.
 * 
 * TODO Design ideas:
 * 
 * Vector2f could extend Tuple2(x, y), but this causes inefficiency:
 * tuple2 will store x,y as objects, causing boxing. Otherwise we could add:
 *   extends Tuple2(x, y) { override def swap = Vector2f(y, x) }
 * 
 * Reconsider with Scala 2.8 @specialized
 * 
 * Idea: provide mutable version for big computations
 */
case class Vector2f(x: Float, y: Float) {
  def +(a: Float) = Vector2f(x + a, y + a)
  def -(a: Float) = Vector2f(x - a, y - a)
  def *(a: Float) = Vector2f(x * a, y * a)
  def /(a: Float) = Vector2f(x / a, y / a)

  def cross(a: Float) = Vector2f(a * y, -a * x)
  def ×(a: Float) = Vector2f(a * y, -a * x)

  def +(v: Vector2f) = Vector2f(x + v.x, y + v.y)
  def -(v: Vector2f) = Vector2f(x - v.x, y - v.y)

  def dot(v: Vector2f) = x * v.x + y * v.y
  def ∙(v: Vector2f) = x * v.x + y * v.y

  def cross(v: Vector2f) = x * v.y - y * v.x
  def ×(v: Vector2f) = x * v.y - y * v.x

  def tangent = Vector2f(y, -x) // = ×(1)
  def unary_- = Vector2f(-x, -y)
  def swap = Vector2f(y, x)
  def abs = Vector2f(x.abs, y.abs)

  /** Polar coordinates */
  def theta = atan2(y, x).toFloat
  def θ = atan2(y, x).toFloat

  def to(v: Vector2f) = new Vector2fRange(this, v, Vector2f.One)
  def times(n: Int) = new Vector2fTimes(this, n, Vector2f.One)

  /**
   * Since normalization is a simple operation, in cases where speed is desired, but the length before normalization is also needed,
   * use this instead:
   * val l = v.length
   * v /= l
   */
  def normalize = this / length

  def length = sqrt(x * x + y * y)
  def lengthSquared = x * x + y * y

  // Unlike with float extensions, calling clamp on an existing vector doesn't effect performance, so this shouldn't be static.
  def clamp(low: Vector2f, high: Vector2f) = Preamble.max(low, Preamble.min(this, high))

  // TODO remove this if Scala 2.8 allows Vector to extend Tuple2 without performance hit
  def tuple = (x, y)

  // Inlined for performance
  def isValid = x != Float.NaN && x != Float.NegativeInfinity && x != Float.PositiveInfinity &&
    y != Float.NaN && y != Float.NegativeInfinity && y != Float.PositiveInfinity

  override def productPrefix = ""
  
  // for debugging only
  Vector2f.creationCount += 1 
}
