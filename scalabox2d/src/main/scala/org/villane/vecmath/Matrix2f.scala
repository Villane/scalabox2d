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
case class Matrix2f(a11: Float, a12: Float, a21: Float, a22: Float) {
  def col1 = Vector2f(a11, a21)
  def col2 = Vector2f(a12, a22)

  // TODO +,-,*,/ Float
  def +(a: Float) = Matrix2f(a11 + a, a12 + a, a21 + a, a22 + a)

  def +(m: Matrix2f) = Matrix2f(a11 + m.a11, a12 + m.a12,
                                a21 + m.a21, a22 + m.a22)

  /**
   * Multiply a vector by this matrix.
   * @param v Vector to multiply by matrix.
   * @return Resulting vector
   */
  def *(v: Vector2f) = Vector2f(a11 * v.x + a12 * v.y,
                                a21 * v.x + a22 * v.y)

  /**
   * Multiply a vector by the transpose of this matrix. (mulT)
   * @param v
   * @return
   */
  def **(v: Vector2f) = Vector2f(a11 * v.x + a21 * v.y,
                                 a12 * v.x + a22 * v.y)

  /**
   * Solve A * x = b where A = this matrix.
   * @return The vector x that solves the above equation.
   */
  def solve(b: Vector2f) = {
    var det = a11 * a22 - a12 * a21
    assert (det != 0.0f)
    det = 1.0f / det
    Vector2f(det * (a22 * b.x - a12 * b.y),
             det * (a11 * b.y - a21 * b.x))
  }

  /**
   * Multiply another matrix by this one.
   */
  // Matrix2f(this * m.col1, this * m.col2)
  def *(m: Matrix2f): Matrix2f = Matrix2f(
    a11 * m.a11 + a12 * m.a21,
    a11 * m.a12 + a12 * m.a22,
    a21 * m.a11 + a22 * m.a21,
    a21 * m.a12 + a22 * m.a22
  )

  /**
   * Multiply another matrix by the transpose of this one.
   */
  def **(m: Matrix2f): Matrix2f = Matrix2f(
    a11 * m.a11 + a21 * m.a21,
    a11 * m.a12 + a21 * m.a22,
    a12 * m.a11 + a22 * m.a21,
    a12 * m.a12 + a22 * m.a22
  )

  def transpose = Matrix2f(a11, a21, a12, a22)

  def abs = Matrix2f(a11.abs, a12.abs, a21.abs, a22.abs)

  def invert = {
    // assert((a*d-b*c) != 0.0f) before dividing with possible zero
    val det = 1 / (a11 * a22 - a12 * a21)
    Matrix2f(det * a22, -det * a12,
             -det * a21, det * a11)
  }

}
