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
    pos.x + rot.a11 * v.x + rot.a12 * v.y, 
    pos.y + rot.a21 * v.x + rot.a22 * v.y
  ) // = pos + (rot * v)
  def **(v: Vector2f) = {
	val v1x = v.x - pos.x
	val v1y = v.y - pos.y
	Vector2f(v1x * rot.a11 + v1y * rot.a21, v1x * rot.a12 + v1y * rot.a22)
  } // = rot ** (v - pos)
}
