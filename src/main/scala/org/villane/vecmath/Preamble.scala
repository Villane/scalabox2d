package org.villane.vecmath

/**
 * The vecmath package includes geometry classes useful for 2D graphics and physics engines.
 * 
 * Vector2f is an immutable implementation of a 2D vector consisting of two floats
 * Matrix2f is an immutable implementation of a 2D matrix consisiting of two vectors 
 * Transform2f is a transformation, consisting of a point (vector) and it's rotation (matrix)
 * MathUtil contains useful math with floats.
 * FloatExtensions allows some operations to be used with the float on the left side.
 */
object Preamble {
  implicit def tuple2f2Vector2f(xy: (Float, Float)) = Vector2f(xy._1, xy._2)
  implicit def tuple2vector2f2Matrix2f(m: (Vector2f, Vector2f)) = Matrix2f(m._1, m._2)
  implicit def float2floatExtensions(a: Float) = new FloatExtensions(a)
}
