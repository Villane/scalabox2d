package org.villane.vecmath

import Preamble._

/**
 * Vector2 creation methods and often used constant values.
 */
object Vector2 {
  val Zero = new Vector2(0, 0)
  val One = Vector2(1, 1)
  val XUnit = Vector2(1, 0)
  val YUnit = Vector2(0, 1)
  def polar(r: Float, theta: Float) = Vector2(cos(theta) * r, sin(theta) * r)

  def lerp(begin: Vector2, end: Vector2, scalar: Float) = Vector2(
    begin.x + scalar * (end.x - begin.x),
    begin.y + scalar * (end.y - begin.y)
  )

  // for debugging only
  var creationCount = 0L
}

/**
 * An immutable 2D Vector represented as x and y coordinates in single precision
 * floating point numbers.
 * 
 * TODO Design ideas:
 * 
 * Vector2 could extend Tuple2(x, y), but this causes inefficiency:
 * tuple2 will store x,y as objects, causing boxing. Otherwise we could add:
 *   extends Tuple2(x, y) { override def swap = Vector2(y, x) }
 * 
 * Reconsider with Scala 2.8 @specialized
 * 
 * Idea: provide mutable version for big computations
 */
case class Vector2(x: Float, y: Float) {
  def +(a: Float) = Vector2(x + a, y + a)
  def -(a: Float) = Vector2(x - a, y - a)
  def *(a: Float) = Vector2(x * a, y * a)
  def /(a: Float) = Vector2(x / a, y / a)

  def cross(a: Float) = Vector2(a * y, -a * x)
  def ×(a: Float) = Vector2(a * y, -a * x)

  def +(v: Vector2) = Vector2(x + v.x, y + v.y)
  def -(v: Vector2) = Vector2(x - v.x, y - v.y)

  def dot(v: Vector2) = x * v.x + y * v.y
  def ∙(v: Vector2) = x * v.x + y * v.y

  def cross(v: Vector2) = x * v.y - y * v.x
  def ×(v: Vector2) = x * v.y - y * v.x

  def normal = Vector2(y, -x) // = ×(1)
  def unary_- = Vector2(-x, -y)
  def swap = Vector2(y, x)
  def abs = Vector2(x.abs, y.abs)

  /** Polar coordinates */
  def theta = atan2(y, x)
  def θ = atan2(y, x)

  def to(v: Vector2) = new Vector2Range(this, v, Vector2.One)
  def times(n: Int) = new Vector2Times(this, n, Vector2.One)

  /**
   * Since normalization is a simple operation, in cases where speed is desired,
   * but the length before normalization is also needed, use this instead:
   * 
   * val len = v.length
   * v /= len
   * 
   */
  def normalize = this / length
  /** @see normalize */
  def unit = this / length

  def length = sqrt(x * x + y * y)
  def lengthSquared = x * x + y * y

  // Unlike with float extensions, calling clamp on an existing vector doesn't effect performance, so this shouldn't be static.
  def clamp(low: Vector2, high: Vector2) = Preamble.max(low, Preamble.min(this, high))

  // TODO remove this if Scala 2.8 allows Vector to extend Tuple2 without performance hit
  def tuple = (x, y)

  // Inlined for performance
  def isValid = x != Float.NaN && x != Float.NegativeInfinity && x != Float.PositiveInfinity &&
    y != Float.NaN && y != Float.NegativeInfinity && y != Float.PositiveInfinity

  override def productPrefix = ""
  
  // for debugging only
  Vector2.creationCount += 1 
}
