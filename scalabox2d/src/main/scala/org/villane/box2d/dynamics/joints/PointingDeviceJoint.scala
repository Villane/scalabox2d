package org.villane.box2d.dynamics.joints

import vecmath._
import vecmath.Preamble._
import dynamics._
import MathUtil.π

//p = attached point, m = mouse point
//C = p - m
//Cdot = v
//   = v + cross(w, r)
//J = [I r_skew]
//Identity used:
//w k % (rx i + ry j) = w * (-ry i + rx j)
class PointingDeviceJoint(defn: PointingDeviceJointDef) extends Joint(defn) {
  private[this] var _target = defn.target
  var localAnchor = body2.transform ** _target 
  var force = Vector2f.Zero
  var mass = Matrix2f.Zero // effective mass for point-to-point constraint.
  var C = Vector2f.Zero // position error

  var maxForce = defn.maxForce
  var beta = 0f // bias factor
  var gamma = 0f // softness

  initPDJ
  def initPDJ {
    val m = body2.mass
    // Frequency
    val ω = 2.0f * π * defn.frequencyHz
    // Damping coefficient
    val d = 2.0f * m * defn.dampingRatio * ω
    // Spring stiffness
    val k = m * ω * ω
    // magic formulas
    gamma = 1.0f / (d + defn.timeStep * k)
    beta = defn.timeStep * k / (d + defn.timeStep * k)
  }

  def target = _target
  /** Use this to update the target point. */
  def target_=(target: Vector2f) {
    if (body2.isSleeping) body2.wakeUp()
    this._target = target
  }

  def anchor1 = target
  def anchor2 = body2.toWorldPoint(localAnchor)
  def reactionForce = force
  def reactionTorque = 0f

  def initVelocityConstraints(step: TimeStep) {
    val b = body2

    // Compute the effective mass matrix.
    val r = b.transform.rot * (localAnchor - b.localCenter)

    // K = [(1/m1 + 1/m2) * eye(2) - skew(r1) * invI1 * skew(r1) - skew(r2)
    // * invI2 * skew(r2)]
    // = [1/m1+1/m2 0 ] + invI1 * [r1.y*r1.y -r1.x*r1.y] + invI2 *
    // [r1.y*r1.y -r1.x*r1.y]
    // [ 0 1/m1+1/m2] [-r1.x*r1.y r1.x*r1.x] [-r1.x*r1.y r1.x*r1.x]
    val invMass = b.invMass
    val invI = b.invI

    val K1 = Matrix2f(invMass, 0.0f, 0.0f, invMass)
    val K2 = Matrix2f(invI * r.y * r.y, -invI * r.x * r.y,
                      -invI * r.x * r.y, invI * r.x * r.x)

	var K = K1 + K2
    K = Matrix2f(K._00 + gamma, K._01,
                 K._10, K._11 + gamma)

    mass = K.invert

    C = (b.sweep.c.x + r.x - target.x, b.sweep.c.y + r.y - target.y)

    // Cheat with some damping
    b.angularVelocity *= 0.98f

    // Warm starting.
    val P = force * step.dt
    b.linearVelocity += P * invMass
    b.angularVelocity += invI * (r.x * P.y - r.y * P.x)
  }

  def solveVelocityConstraints(step: TimeStep) {
    val b = body2

    val r = b.transform.rot * (localAnchor - b.localCenter)

    // Cdot = v + cross(w, r)
    val Cdot = b.linearVelocity + b.angularVelocity × r

    //Vec2 force = -step.inv_dt * Mat22.mul(m_mass, Cdot + (m_beta * step.inv_dt) * m_C + m_gamma * step.dt * m_force);
    var f = Vector2f(Cdot.x + (beta*step.invDt)*C.x + gamma * step.dt * force.x, 
                     Cdot.y + (beta*step.invDt)*C.y + gamma * step.dt * force.y)
    f = mass * f * (-step.invDt)

    val oldForce = force
    force += f
    val forceMagnitude = force.length
    if (forceMagnitude > maxForce) {
      force *= (maxForce / forceMagnitude)
    }
    f = (force.x - oldForce.x, force.y - oldForce.y)

    val P = f * step.dt
    b.linearVelocity += P * b.invMass
    b.angularVelocity += b.invI * r × P
  }

  def solvePositionConstraints() = true
}
