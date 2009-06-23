package org.villane.box2d.dynamics

object Material {
  val Default = Material(0.2f, 0f, 0f)
}

/**
 * A material that can be applied to a fixture definition.
 */
case class Material(
  /** The friction coefficient, usually in the range [0,1]. */
  friction: Float,
  /** The restitution (elasticity) usually in the range [0,1]. */
  restitution: Float,
  /** The density, usually in kg/m^2. */
  density: Float
) {
  def depth(z: Float) = Material(friction, restitution, density * z)
}
