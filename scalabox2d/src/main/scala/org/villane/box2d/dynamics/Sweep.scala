package org.villane.box2d.dynamics

import Settings.ε
import vecmath._

/**
 * Primarily for internal use.
 * <BR><BR>
 * Describes the motion of a body/shape for TOI computation.
 * Shapes are defined with respect to the body origin, which may
 * not coincide with the center of mass. However, to support dynamics
 * we must interpolate the center of mass position.
 *
 * TODO move out of the vecmath package -- this is pretty much tied to the physics engine.
 */
class Sweep {
  /** Local center of mass position */
  var localCenter = Vector2f.Zero 
  /** Center world positions */
  var c0, c = Vector2f.Zero
  /** World angles */
  var a0, a = 0f 
  /** Time interval = [t0,1], where t0 is in [0,1] */
  var t0 = 0f
	
  override def toString() = {
	var s = "Sweep:\nlocalCenter: "+localCenter+"\n";
	s += "c0: "+c0+", c: "+c+"\n";
	s += "a0: "+a0+", a: "+a+"\n";
	s += "t0: "+t0+"\n";
	s
  }

  /**
   * Get the interpolated transform at a specific time.
   * @param t the normalized time in [0,1].
   */
  def getTransform(t: Float) = {
    // center = p + R * localCenter
    val xf = if (1f - t0 > ε) {
      val α = (t - t0) / (1f - t0)
      val pos = c0 * (1f - α) + c * α
      val angle = (1f - α) * a0 + α * a
      Transform2f(pos, angle)
    } else {
      Transform2f(c, a)
    }

    // Shift to origin
    Transform2f(xf.pos - (xf.rot * localCenter), xf.rot) 
  }

  /** 
   * Advance the sweep forward, yielding a new initial state.
   * @param t the new initial time.
   */
  def advance(t: Float) {
    if (t0 < t && 1.0f - t0 > ε) {
      val α = (t - t0) / (1.0f - t0)
      c0 = c0 * (1.0f - α) + c * α 
      a0 = (1.0f - α) * a0 + α * a
      t0 = t
	}
  }

}
