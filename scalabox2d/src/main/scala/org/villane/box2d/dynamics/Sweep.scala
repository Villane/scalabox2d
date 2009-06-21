package org.villane.box2d.dynamics

import vecmath._
import vecmath.Preamble._
import Settings.Epsilon

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
  var localCenter = Vector2.Zero 
  /** Center world positions */
  var c0, c = Vector2.Zero
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
    val xf = if (1f - t0 > Settings.Epsilon) {
      val alpha = (t - t0) / (1f - t0)
      val oneLessAlpha = 1f - alpha
      //val pos = c0 * (1f - alpha) + c * alpha
      val posx = c0.x * oneLessAlpha + c.x * alpha
      val posy = c0.y * oneLessAlpha + c.y * alpha
      val angle = oneLessAlpha * a0 + alpha * a
      Transform2(Vector2(posx, posy), angle)
    } else {
      Transform2(c, a)
    }

    // Shift to origin
    Transform2(xf.pos - (xf.rot * localCenter), xf.rot) 
  }

  /** 
   * Advance the sweep forward, yielding a new initial state.
   * @param t the new initial time.
   */
  def advance(t: Float) {
    if (t0 < t && 1.0f - t0 > Epsilon) {
      val alpha = (t - t0) / (1.0f - t0)
      c0 = c0 * (1.0f - alpha) + c * alpha 
      a0 = (1.0f - alpha) * a0 + alpha * a
      t0 = t
	}
  }

}
