package org.villane.box2d.dynamics

/**
 * This interface allows registration within a JBox2d World
 * to be run immediately after the physics step.  This is
 * useful if you need to do something every step, but would
 * prefer not to have to manually code your step routine 
 * differently, instead letting the engine handle the calling.
 */
trait Steppable {
  def step(dt: Float, iterations: Int)
}