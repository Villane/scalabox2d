package org.villane.box2d.dynamics.joints

import vecmath._
import vecmath.Preamble._

/**
 * Definition for a distance joint.  A distance joint
 * keeps two points on two bodies at a constant distance
 * from each other.
 */
class DistanceJointDef extends JointDef {
  /** The local anchor point relative to body1's origin. */
  var localAnchor1 = Vector2.Zero

  /** The local anchor point relative to body2's origin. */
  var localAnchor2 = Vector2.Zero

  /** The equilibrium length between the anchor points. */
  var length: Scalar = 1.0f

  var frequencyHz: Scalar = 0f

  var dampingRatio: Scalar = 0f

  /**
   * Initialize the bodies, anchors, and length using the world
   * anchors.
   * @param b1 First body
   * @param b2 Second body
   * @param anchor1 World anchor on first body
   * @param anchor2 World anchor on second body
   */
  def this(b1: Body, b2: Body, anchor1: Vector2, anchor2: Vector2) = {
    this()
    body1 = b1
    body2 = b2
    localAnchor1 = body1.toLocalPoint(anchor1)
    localAnchor2 = body2.toLocalPoint(anchor2)
    length = (anchor2 - anchor1).length
  }

}
