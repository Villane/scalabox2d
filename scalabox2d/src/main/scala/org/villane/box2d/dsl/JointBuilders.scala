package org.villane.box2d.dsl

import vecmath._
import vecmath.Preamble._
import shapes._
import collision._
import dynamics._
import dynamics.joints._

class JointBuilder(j: JointDef, bodies: (Body, Body)) {
  j.body1 = bodies._1
  j.body2 = bodies._2
  def collideConnected(v: Boolean) = { j.collideConnected = v; this }
  def userData(v: AnyRef) = { j.userData = v; this }
  def define = j
}

class DistanceJointBuilder(j: DistanceJointDef, bodies: (Body, Body))
  extends JointBuilder(j, bodies) {
  def localAnchor1(v: Vector2) = { j.localAnchor1 = v; this }
  def localAnchor2(v: Vector2) = { j.localAnchor2 = v; this }
  def length(v: Scalar) = { j.length = v; this }
  def frequencyHz(v: Scalar) = { j.frequencyHz = v; this }
  def dampingRatio(v: Scalar) = { j.dampingRatio = v; this }
}

class RevoluteJointBuilder(j: RevoluteJointDef, bodies: (Body, Body))
  extends JointBuilder(j, bodies) {
  def localAnchor1(v: Vector2) = { j.localAnchor1 = v; this }
  def localAnchor2(v: Vector2) = { j.localAnchor2 = v; this }
  def referenceAngle(v: Scalar) = { j.referenceAngle = v; this }
  def anchor(v: Vector2) = {
    j.localAnchor1 = j.body1.toLocalPoint(v)
    j.localAnchor2 = j.body2.toLocalPoint(v)
    j.referenceAngle = j.body2.angle - j.body1.angle
    this
  }
  def enableLimit(v: Boolean) = { j.enableLimit = v; this }
  def lowerAngle(v: Scalar) = { j.lowerAngle = v; this }
  def upperAngle(v: Scalar) = { j.upperAngle = v; this }
  def enableMotor(v: Boolean) = { j.enableMotor = v; this }
  def motorSpeed(v: Scalar) = { j.motorSpeed = v; this }
  def maxMotorTorque(v: Scalar) = { j.maxMotorTorque = v; this }
}
