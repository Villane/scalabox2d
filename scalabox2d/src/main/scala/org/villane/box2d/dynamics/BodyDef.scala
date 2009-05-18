package org.villane.box2d.dynamics

import vecmath.Vector2f
import shapes.Mass

class BodyDef {
  var userData: AnyRef = null
  var mass = Mass.Zero
  var pos = Vector2f.Zero
  var angle = 0f

  /**
   * Linear damping is use to reduce the linear velocity. The damping parameter
   * can be larger than 1.0f but the damping effect becomes sensitive to the
   * time step when the damping parameter is large.
   */
  var linearDamping = 0f

  /**
   * Angular damping is use to reduce the angular velocity. The damping parameter
   * can be larger than 1.0f but the damping effect becomes sensitive to the
   * time step when the damping parameter is large.
   */
  var angularDamping = 0f

  /**
   * Set this flag to false if this body should never fall asleep.  Note that
   * this increases CPU usage.
   */
  var allowSleep = true

  /** Is this body initially sleeping? */
  var isSleeping = false

  /** Should this body be prevented from rotating?  Useful for characters. */
  var fixedRotation = false

  /**
   * Is this a fast moving body that should be prevented from tunneling through
   * other moving bodies? Note that all bodies are prevented from tunneling through
   * static bodies.
   * <BR><BR><em>Warning</em>: You should use this flag sparingly since it increases processing time.
   */
  var isBullet = false

}
