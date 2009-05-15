package org.villane.box2d.dynamics

/** This is called when a body's shape passes outside of the world boundary. */
trait BoundaryListener {
  /**
   * This is called for each body that leaves the world boundary.
   * <BR><BR><em>Warning</em>: you can't modify the world inside this callback.
   */
  def violation(body: Body)
}
