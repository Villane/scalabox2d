package org.villane.vecmath

object Transform2 {
  val Identity = Transform2(Vector2.Zero, Matrix22.Identity)

  /**
   * Creates a transform from a position and an angle.
   */
  def apply(pos: Vector2, angle: Float): Transform2 = Transform2(pos, Matrix22.rotation(angle))
}

case class Transform2(
  /** Translation component of the transformation */
  pos: Vector2,
  /** Rotation component of the transformation */
  rot: Matrix22
) {
  def *(v: Vector2) = Vector2(
    pos.x + rot.a11 * v.x + rot.a12 * v.y, 
    pos.y + rot.a21 * v.x + rot.a22 * v.y
  ) // = pos + (rot * v)
  def **(v: Vector2) = {
    val v1x = v.x - pos.x
    val v1y = v.y - pos.y
    Vector2(v1x * rot.a11 + v1y * rot.a21, v1x * rot.a12 + v1y * rot.a22)
  } // = rot ** (v - pos)
}
