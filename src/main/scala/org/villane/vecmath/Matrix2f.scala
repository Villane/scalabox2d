package org.villane.vecmath

object Matrix2f {
  val Zero = Matrix2f(Vector2f.Zero, Vector2f.Zero)
  val One = Matrix2f(Vector2f.One, Vector2f.One)
  val Identity = Matrix2f(Vector2f.XUnit, Vector2f.YUnit)

  def apply(col1x: Float, col2x: Float, col1y: Float, col2y: Float): Matrix2f = 
    Matrix2f(Vector2f(col1x, col1y), Vector2f(col2x, col2y))

  /**
   * Creates a rotation matrix from an angle.
   * 
   * @param angle rotation in radians
   */
  def rotation(angle: Float) = {
    val c = MathUtil.cos(angle)
    val s = MathUtil.sin(angle)
    Matrix2f(Vector2f(c, s), Vector2f(-s, c))
  }
}

/**
 * A 2-by-2 matrix. Stored in column-major order.
 * 
 * Design note: Matrix2f could conceptually extend Tuple2 of vectors, but does not for efficiency.
 * See the same note about Vector2f for details.
 */
case class Matrix2f(col1: Vector2f, col2: Vector2f) {
  def +(m: Matrix2f) = Matrix2f(col1 + m.col1, col2 + m.col2)

  /**
   * Multiply a vector by this matrix.
   * @param v Vector to multiply by matrix.
   * @return Resulting vector
   */
  def *(v: Vector2f) = Vector2f(col1.x * v.x + col2.x * v.y, col1.y * v.x + col2.y * v.y)
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
    val (a11,a12,a21,a22) = (col1.x, col2.x, col1.y, col2.y)
    var det = a11 * a22 - a12 * a21
    assert (det != 0.0f)
    det = 1.0f / det
    Vector2f(det * (a22 * b.x - a12 * b.y),
             det * (a11 * b.y - a21 * b.x))
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
  
  def abs = Matrix2f(col1.abs, col2.abs)
  def invert = {
    val (a,b,c,d) = (col1.x, col2.x, col1.y, col2.y)
    // assert((a*d-b*c) != 0.0f) before dividing with possible zero
    val det = 1 / (a * d - b * c)
    Matrix2f(Vector2f(det * d, -det * c),
             Vector2f(-det * b, det * a))
  }

}
