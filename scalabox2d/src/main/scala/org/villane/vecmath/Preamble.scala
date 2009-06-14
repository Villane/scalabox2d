package org.villane.vecmath

/**
 * The vecmath package includes geometry classes useful for 2D graphics and
 * physics engines.
 *
 * This preamble contains implicit conversions from tuples to vectors and matrices
 * and extension methods for Scalar.
 * 
 * Vector2 is an immutable implementation of a 2D vector of two floats
 * Matrix22 is an immutable implementation of a 2D matrix of two vectors 
 * Transform2 is a transformation, consisting of a point (vector) and it's rotation (matrix)
 * MathUtil contains useful math with floats.
 * ScalarExtensions allows some operations to be used with the float on the left side.
 */
object Preamble extends FloatMath {
  implicit def tuple2ScalarToVector2(xy: (Scalar, Scalar)) = Vector2(xy._1, xy._2)
  implicit def tuple2Vector2ToMatrix22(m: (Vector2, Vector2)) = Matrix22(m._1, m._2)
  implicit def scalarExtensions(a: Scalar) = new ScalarExtensions(a)

  def min(a: Vector2, b: Vector2): Vector2 = Vector2(min(a.x, b.x), min(a.y, b.y))
  def min(a: Vector2, b: Vector2, c: Vector2, d: Vector2): Vector2 = Vector2(
    min(a.x, b.x, c.x, d.x),
    min(a.y, b.y, c.y, d.y)
  )
  def max(a: Vector2, b: Vector2): Vector2 = Vector2(max(a.x, b.x), max(a.y, b.y))
  def max(a: Vector2, b: Vector2, c: Vector2, d: Vector2): Vector2 = Vector2(
    max(a.x, b.x, c.x, d.x),
    max(a.y, b.y, c.y, d.y)
  )
}
