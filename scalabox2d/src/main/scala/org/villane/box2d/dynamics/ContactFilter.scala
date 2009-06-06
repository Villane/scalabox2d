package org.villane.box2d.dynamics

import shapes.Shape

/**
 * Implement this class to provide collision filtering. In other words, you can implement
 * this class if you want finer control over contact creation.
 */
trait ContactFilter {
  /**
   * Return true if contact calculations should be performed between these two shapes.
   * @warning for performance reasons this is only called when the AABBs begin to overlap.
   */
  def shouldCollide(fixture1: Fixture, fixture2: Fixture): Boolean
}
