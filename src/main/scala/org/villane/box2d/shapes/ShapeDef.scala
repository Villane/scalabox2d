package org.villane.box2d.shapes

/** 
 * Superclass for shape definitions.
 *
 * You should usually use CircleDef or PolygonDef to define concrete shapes.
 * 
 * Shape definitions are mutable to make them easier to build.
 */
abstract class ShapeDef {
  /** Use this to store application specify shape data. */
  var userData: AnyRef = null

  /** The shape's friction coefficient, usually in the range [0,1]. */
  var friction = 0.2f

  /** The shape's restitution (elasticity) usually in the range [0,1]. */
  var restitution = 0f

  /** The shape's density, usually in kg/m^2. */
  var density = 0f

  /** Contact filtering data. */
  var filter = FilterData.Default

  /** A sensor shape collects contact information but never generates a collision response. */
  var isSensor = false
}