package org.villane.vecmath

object Vector2f {
  val Zero = new Vector2f(0, 0)
  val One = Vector2f(1, 1)
  val XUnit = Vector2f(1, 0)
  val YUnit = Vector2f(0, 1)
  def min(a: Vector2f, b: Vector2f) = Vector2f(MathUtil.min(a.x, b.x), MathUtil.min(a.y, b.y))
  def max(a: Vector2f, b: Vector2f) = Vector2f(MathUtil.max(a.x, b.x), MathUtil.max(a.y, b.y))
  def polar(r: Float, theta: Float) = Vector2f(MathUtil.cos(theta) * r, MathUtil.sin(theta) * r)

  // for debugging only
  var count = 0L
}

/**
 * This is an implementation of immutable 2D vector and matrix. JBox2D was used for inspiration.
 * 
 * Design notes:
 * 
 * Vector2f could extend Tuple2(x, y), but this causes inefficiency:
 * tuple2 will store x,y as objects, causing boxing. Otherwise we could add:
 *   extends Tuple2(x, y) { override def swap = Vector2f(y, x) }
 * 
 * Idea: provide mutable version for big computations
 */
case class Vector2f(x: Float, y: Float) {
  @inline def +(a: Float) = Vector2f(x + a, y + a)
  @inline def -(a: Float) = Vector2f(x - a, y - a)
  @inline def *(a: Float) = Vector2f(x * a, y * a)
  @inline def /(a: Float) = Vector2f(x / a, y / a)

  @inline def cross(a: Float) = Vector2f(a * y, -a * x)
  @inline def ×(a: Float) = Vector2f(a * y, -a * x)

  @inline def +(v: Vector2f) = Vector2f(x + v.x, y + v.y)
  @inline def -(v: Vector2f) = Vector2f(x - v.x, y - v.y)

  @inline def dot(v: Vector2f) = x * v.x + y * v.y
  @inline def ∙(v: Vector2f) = x * v.x + y * v.y

  @inline def cross(v: Vector2f) = x * v.y - y * v.x
  @inline def ×(v: Vector2f) = x * v.y - y * v.x

  @inline def tangent = Vector2f(y, -x) // = ×(1)
  @inline def unary_- = Vector2f(-x, -y)
  @inline def swap = Vector2f(y, x)
  @inline def abs = Vector2f(x.abs, y.abs)

  /** Polar coordinates */
  @inline def theta = Math.atan2(y, x).toFloat
  @inline def θ = Math.atan2(y, x).toFloat

  /**
   * Since normalization is a simple operation, in cases where speed is desired, but the length before normalization is also needed,
   * use this instead:
   * val l = v.length
   * v /= l
   */
  @inline def normalize = this / length

  @inline def length = MathUtil.sqrt(x * x + y * y)
  @inline def lengthSquared = x * x + y * y

  // Unlike with float extensions, calling clamp on an existing vector doesn't affect performance, so this shouldn't be static.
  @inline def clamp(low: Vector2f, high: Vector2f) = Vector2f.max(low, Vector2f.min(this, high))

  def tuple = (x, y)

  // Inlined for performance
  def isValid = x != Float.NaN && x != Float.NegativeInfinity && x != Float.PositiveInfinity &&
    y != Float.NaN && y != Float.NegativeInfinity && y != Float.PositiveInfinity

  override def productPrefix = ""
  
  // for debugging only
  Vector2f.count += 1 
}
