package org.villane.box2d.dynamics

import shapes.Shape
import joints.Joint

/**
 * Joints and shapes are destroyed when their associated
 * body is destroyed. Implement this listener so that you
 * may nullify references to these joints and shapes.
 */
trait DestructionListener {
  /**
   * Called when any joint is about to be destroyed due
   * to the destruction of one of its attached bodies.
   */
  def sayGoodbye(joint: Joint)

  /**
   * Called when any fixture is about to be destroyed due
   * to the destruction of its parent body.
   */
  def sayGoodbye(fixture: Fixture)
}
