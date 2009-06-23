package org.villane.box2d.dynamics

import shapes.ShapeDef

/**
 * A fixture definition is used to create a fixture. This class defines an
 * abstract fixture definition. You can reuse fixture definitions safely.
 */
case class FixtureDef(var shapeDef: ShapeDef) {
  /** The friction coefficient, usually in the range [0,1]. */
  var friction = 0.2f

  /** The restitution (elasticity) usually in the range [0,1]. */
  var restitution = 0f

  /** The density, usually in kg/m^2. */
  var density = 0f

  /** A sensor collects contact information but never generates a collision response. */
  var isSensor = false

  /** Contact filtering data. */
  var filter = FilterData.Default

  /** Use this to store application specific fixture data. */
  var userData: AnyRef = null

  /** Apply the given material to this fixture definition. */
  def apply(m: Material) = {
    friction = m.friction
    density = m.density
    restitution = m.restitution
  }

}
