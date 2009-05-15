package org.villane.box2d.dynamics

import shapes._

/**
 * Default contact filter, using groupIndex, maskBits and categoryBits as detailed
 * in Box2d manual.
 */
object DefaultContactFilter extends ContactFilter {
  /**
   * Return true if contact calculations should be performed between these two shapes.
   * If you implement your own collision filter you may want to build from this implementation.
   */
  def shouldCollide(shape1: Shape, shape2: Shape) = {
    val filter1 = shape1.filter
    val filter2 = shape2.filter

    if (filter1.groupIndex == filter2.groupIndex && filter1.groupIndex != 0)
      filter1.groupIndex > 0
    else
      (filter1.maskBits & filter2.categoryBits) != 0 && (filter1.categoryBits & filter2.maskBits) != 0
  }
}
