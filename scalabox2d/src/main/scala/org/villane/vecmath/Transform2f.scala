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
    pos.x + rot._00 * v.x + rot._01 * v.y, 
    pos.y + rot._10 * v.x + rot._11 * v.y
  ) // = pos + (rot * v)
  def **(v: Vector2f) = {
	val v1x = v.x - pos.x
	val v1y = v.y - pos.y
	Vector2f(v1x * rot._00 + v1y * rot._10, v1x * rot._01 + v1y * rot._11)
  } // = rot ** (v - pos)
}
