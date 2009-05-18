package org.villane.box2d.dynamics.contacts

/**
 * Implement this class to get collision results. You can use these results for
 * things like sounds and game logic. You can also get contact results by
 * traversing the contact lists after the time step. However, you might miss
 * some contacts because continuous physics leads to sub-stepping.
 * Additionally you may receive multiple callbacks for the same contact in a
 * single time step.
 * You should strive to make your callbacks efficient because there may be
 * many callbacks per time step.
 * <BR><BR><em>Warning</em>: The contact separation is the last computed value.
 * <BR><BR><em>Warning</em>: You cannot create/destroy Box2D entities inside these callbacks.
 * Buffer any such events and apply them at the end of the time step.
 */
trait ContactListener {
  /**
   * Called when a contact point is added. This includes the geometry
   * and the forces.
   */
  def add(point: ContactPoint)

  /**
   * Called when a contact point persists. This includes the geometry
   * and the forces.
   */
  def persist(point: ContactPoint)

  /**
   * Called when a contact point is removed. This includes the last
   * computed geometry and forces.
   */
  def remove(point: ContactPoint)

  def result(point: ContactResult)
}
