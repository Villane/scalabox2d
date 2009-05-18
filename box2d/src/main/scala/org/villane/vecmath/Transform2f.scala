package org.villane.vecmath

object Transform2f {
  val Identity = Transform2f(Vector2f.Zero, Matrix2f.Identity)

  /**
   * Creates a transform from a position and an angle.
   */
  def apply(pos: Vector2f, angle: Float): Transform2f = Transform2f(pos, Matrix2f.rotation(angle))
}

case class Transform2f(pos: Vector2f, rot: Matrix2f) {
  def *(v: Vector2f) = Vector2f(
    pos.x + rot.col1.x * v.x + rot.col2.x * v.y, 
    pos.y + rot.col1.y * v.x + rot.col2.y * v.y
  ) // = pos + (rot * v)
  def **(v: Vector2f) = {
	val v1x = v.x-pos.x
	val v1y = v.y-pos.y
	val b = rot.col1
	val b1 = rot.col2
	Vector2f(v1x * b.x + v1y * b.y, v1x * b1.x + v1y * b1.y)
  } // = rot ** (v - pos)
}
