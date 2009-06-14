package org.villane.box2d.dynamics.joints

import vecmath._
import vecmath.Preamble._

object Jacobian {
  val Zero = Jacobian(Vector2.Zero, 0, Vector2.Zero, 0)
}

case class Jacobian(
  linear1: Vector2,
  angular1: Scalar,
  linear2: Vector2,
  angular2: Scalar
) {
  def compute(x1: Vector2, a1: Scalar, x2: Vector2, a2: Scalar) =
    linear1 ∙ x1 + angular1 * a1 + linear2 ∙ x2 + angular2 * a2
}
