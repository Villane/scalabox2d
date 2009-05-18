package org.villane.box2d.dynamics.joints

case class JointEdge(
  /** Provides quick access to the other body attached. */
  var other: Body,
  /** The joint. */
  var joint: Joint
)
