package org.villane.vecmath

object Matrix2f {
  val Zero = Matrix2f(0, 0, 0, 0)
  val One = Matrix2f(1, 1, 1, 1)
  val Identity = Matrix2f(1, 0, 0, 1)

  def apply(col1: Vector2f, col2: Vector2f): Matrix2f =
    Matrix2f(col1.x, col2.x, col1.y, col2.y)

  /**
   * Creates a rotation matrix from an angle.
   * 
   * @param angle rotation in radians
   */
  def rotation(angle: Float) = {
    val c = MathUtil.cos(angle)
    val s = MathUtil.sin(angle)
    Matrix2f(c, -s, s, c)
  }
}

/**
 * A 2-by-2 matrix. Stored in column-major order.
 * 
 * Design note: Matrix2f could conceptually extend Tuple2 of vectors, but does not for efficiency.
 * See the same note about Vector2f for details.
 */
case class Matrix2f(_00: Float, _01: Float, _10: Float, _11: Float) {
  def col1 = Vector2f(_00, _10)
  def col2 = Vector2f(_01, _11)

  // TODO +,-,*,/ Float
  def +(a: Float) = Matrix2f(_00 + a, _01 + a, _10 + a, _11 + a)

  def +(m: Matrix2f) = Matrix2f(_00 + m._00, _01 + m._01,
                                _10 + m._10, _11 + m._11)

  /**
   * Multiply a vector by this matrix.
   * @param v Vector to multiply by matrix.
   * @return Resulting vector
   */
  def *(v: Vector2f) = Vector2f(_00 * v.x + _01 * v.y,
                                _10 * v.x + _11 * v.y)

  /**
   * Multiply a vector by the transpose of this matrix. (mulT)
   * @param v
   * @return
   */
  def **(v: Vector2f) = Vector2f(v ∙ col1, v ∙ col2)

  /**
   * Solve A * x = b where A = this matrix.
   * @return The vector x that solves the above equation.
   */
  def solve(b: Vector2f) = {
    var det = _00 * _11 - _01 * _10
    assert (det != 0.0f)
    det = 1.0f / det
    Vector2f(det * (_11 * b.x - _01 * b.y),
             det * (_00 * b.y - _10 * b.x))
  }

  /**
   * Multiply another matrix by this one (this one on left).
   * @param m
   * @return
   */
  def *(m: Matrix2f): Matrix2f = Matrix2f(this * m.col1, this * m.col2)

  /**
   * Multiply another matrix by the transpose of this one (transpose of this one on left).
   * @param B
   * @return
   */
  def **(m: Matrix2f): Matrix2f = Matrix2f(Vector2f(col1 ∙ m.col1, col2 ∙ m.col1),
                                           Vector2f(col1 ∙ m.col2, col2 ∙ m.col2))
  //TODO def transpose = Matrix2f(Vector2f(col1.x, col2.x), Vector2f(col1.y, col2.y))

  def abs = Matrix2f(col1.abs, col2.abs)
  def invert = {
    // assert((a*d-b*c) != 0.0f) before dividing with possible zero
    val det = 1 / (_00 * _11 - _01 * _10)
    Matrix2f(Vector2f(det * _11, -det * _10),
             Vector2f(-det * _01, det * _00))
  }

}
