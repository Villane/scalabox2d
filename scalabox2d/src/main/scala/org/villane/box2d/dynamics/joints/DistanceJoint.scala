package org.villane.box2d.dynamics.joints

import vecmath.Vector2f
import vecmath.Preamble._
import Settings.ε
import vecmath.MathUtil.π

//C = norm(p2 - p1) - L
//u = (p2 - p1) / norm(p2 - p1)
//Cdot = dot(u, v2 + cross(w2, r2) - v1 - cross(w1, r1))
//J = [-u -cross(r1, u) u cross(r2, u)]
//K = J * invM * JT
//= invMass1 + invI1 * cross(r1, u)^2 + invMass2 + invI2 * cross(r2, u)^2

/**
 * A distance joint constrains two points on two bodies
 * to remain at a fixed distance from each other. You can view
 * this as a massless, rigid rod.
 */
class DistanceJoint(defn: DistanceJointDef) extends Joint(defn) {
  val localAnchor1 = defn.localAnchor1
  val localAnchor2 = defn.localAnchor2
  val length = defn.length
  var impulse = 0.0f
  var u = Vector2f.Zero
  // effective mass for the constraint.
  var mass = 0.0f
  val frequencyHz = defn.frequencyHz
  val dampingRatio = defn.dampingRatio
  var gamma = 0.0f
  var bias = 0.0f

  def anchor1 = body1.toWorldPoint(localAnchor1)
  def anchor2 = body2.toWorldPoint(localAnchor2)
  def reactionForce = (impulse * u.x, impulse * u.y)
  def reactionTorque = 0.0f

  def initVelocityConstraints(step: TimeStep) {
	invDt = step.invDt

    //TODO: fully inline temp Vec2 ops
    val b1 = body1
    val b2 = body2

    // Compute the effective mass matrix.
    val r1 = b1.transform.rot * (localAnchor1 - b1.localCenter)
    val r2 = b2.transform.rot * (localAnchor2 - b2.localCenter)
    u = (b2.sweep.c.x + r2.x - b1.sweep.c.x - r1.x,
         b2.sweep.c.y + r2.y - b1.sweep.c.y - r1.y)

    // Handle singularity.
    val len = u.length
    if (len > Settings.linearSlop) {
      u /= len
    } else {
      u = Vector2f.Zero
    }

    val cr1u = r1 × u
    val cr2u = r2 × u

    val invMass = b1.invMass + b1.invI * cr1u * cr1u + b2.invMass + b2.invI * cr2u * cr2u
    assert(invMass > ε)
    mass = 1.0f / invMass

    if (frequencyHz > 0.0f) {
      val C = len - length

      // Frequency
      val ω = 2 * π * frequencyHz

      // Damping coefficient
      val d = 2.0f * mass * dampingRatio * ω

      // Spring stiffness
      val k = mass * ω * ω

      // magic formulas
      gamma = 1.0f / (step.dt * (d + step.dt * k))
      bias = C * step.dt * k * gamma
      mass = 1.0f / (invMass + gamma)
    }

    if (step.warmStarting) {
      impulse *= step.dtRatio
      val P = u * impulse
      b1.linearVelocity -= P * b1.invMass
      b1.angularVelocity -= b1.invI * r1 × P
      b2.linearVelocity += P * b2.invMass
      b2.angularVelocity += b2.invI * r2 × P
    } else {
      impulse = 0.0f
    }
  }

  def solvePositionConstraints(): Boolean = {
    if (frequencyHz > 0.0f) {
      return true
    }

    val b1 = body1
    val b2 = body2

    val r1 = b1.transform.rot * (localAnchor1 - b1.localCenter)
    val r2 = b2.transform.rot * (localAnchor2 - b2.localCenter)

    var d = Vector2f(b2.sweep.c.x + r2.x - b1.sweep.c.x - r1.x,
                     b2.sweep.c.y + r2.y - b1.sweep.c.y - r1.y)

    val len = d.length
    d = d.normalize
    val C = (len - length).clamp(-Settings.maxLinearCorrection, Settings.maxLinearCorrection)

    val imp = -mass * C
    u = d
    val P = imp * u

    b1.sweep.c -= P * b1.invMass
    b1.sweep.a -= b1.invI * (r1.x*P.y-r1.y*P.x)//(r1 × P);
    b2.sweep.c += P * b2.invMass
    b2.sweep.a += b2.invI * (r2.x*P.y-r2.y*P.x)//(r2 × P);

    b1.synchronizeTransform()
    b2.synchronizeTransform()

    return C.abs < Settings.linearSlop
  }

  def solveVelocityConstraints(step: TimeStep) {
    val b1 = body1
    val b2 = body2

    val r1 = b1.transform.rot * (localAnchor1 - b1.localCenter)
    val r2 = b2.transform.rot * (localAnchor2 - b2.localCenter)

    // Cdot = dot(u, v + cross(w, r))
    val v1 = b1.linearVelocity + b1.angularVelocity × r1
    val v2 = b2.linearVelocity + b2.angularVelocity × r2
    val Cdot = u ∙ (v2 - v1)
    	
    val imp = -mass * (Cdot + bias + gamma * impulse)
    impulse += imp

    val P = u * imp
    b1.linearVelocity -= P * b1.invMass
    b1.angularVelocity -= b1.invI * (r1.x*P.y - r1.y*P.x)//(r1 × P);
    b2.linearVelocity += P * b2.invMass
    b2.angularVelocity += b2.invI * (r2.x*P.y - r2.y*P.x)//(r2 × P);
  }
}
