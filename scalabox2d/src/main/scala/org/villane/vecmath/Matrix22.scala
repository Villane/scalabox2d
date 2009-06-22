package org.villane.vecmath

import Preamble._

object Matrix22 {
  val Zero = Matrix22(0, 0, 0, 0)
  val One = Matrix22(1, 1, 1, 1)
  val Identity = Matrix22(1, 0, 0, 1)

  def apply(col1: Vector2, col2: Vector2): Matrix22 =
    Matrix22(col1.x, col2.x, col1.y, col2.y)

  /**
   * Creates a rotation matrix from an angle.
   * 
   * @param angle rotation in radians
   */
  def rotation(angle: Float) = {
    val c = cos(angle)
    val s = sin(angle)
    Matrix22(c, -s, s, c)
  }
}

/**
 * A 2-by-2 matrix. Stored in row-major order.
 * 
 * Design note: Matrix22 could conceptually extend Tuple4, but does not for efficiency.
 * See the same note about Vector2 for details.
 * 
 * TODO compute eigenvalues / max eigenvalue
 */
case class Matrix22(a11: Float, a12: Float, a21: Float, a22: Float) {
  def col1 = Vector2(a11, a21)
  def col2 = Vector2(a12, a22)

  // TODO +,-,*,/ Float
  def +(a: Float) = Matrix22(a11 + a, a12 + a, a21 + a, a22 + a)

  def +(m: Matrix22) = Matrix22(a11 + m.a11, a12 + m.a12,
                                a21 + m.a21, a22 + m.a22)

  /**
   * Multiply a vector by this matrix.
   * @param v Vector to multiply by matrix.
   * @return Resulting vector
   */
  def *(v: Vector2) = Vector2(a11 * v.x + a12 * v.y,
                              a21 * v.x + a22 * v.y)

  /**
   * Multiply a vector by the transpose of this matrix. (mulT)
   * @param v
   * @return
   */
  def **(v: Vector2) = Vector2(a11 * v.x + a21 * v.y,
                               a12 * v.x + a22 * v.y)

  def determinant = a11 * a22 - a12 * a21

  /**
   * Solve A * x = b where A = this matrix.
   * @return The vector x that solves the above equation.
   */
  def solve(b: Vector2) = {
    var det = determinant
    assert (det != 0.0f)
    det = 1 / det
    Vector2(det * (a22 * b.x - a12 * b.y),
            det * (a11 * b.y - a21 * b.x))
  }

  /**
   * Multiply another matrix by this one.
   */
  // Matrix22(this * m.col1, this * m.col2)
  def *(m: Matrix22): Matrix22 = Matrix22(
    a11 * m.a11 + a12 * m.a21,
    a11 * m.a12 + a12 * m.a22,
    a21 * m.a11 + a22 * m.a21,
    a21 * m.a12 + a22 * m.a22
  )

  /**
   * Multiply another matrix by the transpose of this one.
   */
  def **(m: Matrix22): Matrix22 = Matrix22(
    a11 * m.a11 + a21 * m.a21,
    a11 * m.a12 + a21 * m.a22,
    a12 * m.a11 + a22 * m.a21,
    a12 * m.a12 + a22 * m.a22
  )

  def transpose = Matrix22(a11, a21, a12, a22)

  def abs = Matrix22(Math.abs(a11), Math.abs(a12),
                     Math.abs(a21), Math.abs(a22))

  def invert = {
    var det = determinant
    assert (det != 0.0f)
    det = 1 / det
    Matrix22(det * a22, -det * a12,
             -det * a21, det * a11)
  }

}
