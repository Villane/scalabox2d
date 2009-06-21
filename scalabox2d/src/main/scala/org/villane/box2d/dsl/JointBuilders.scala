package org.villane.box2d.dsl

import vecmath._
import vecmath.Preamble._
import shapes._
import collision._
import dynamics._
import dynamics.joints._

class JointBuilder(j: JointDef) {
  def collideConnected(v: Boolean) = { j.collideConnected = v; this }
  def userData(v: AnyRef) = { j.userData = v; this }
  def define = j
}

class BodyJointBuilder(j: JointDef, bodies: (Body, Body))
  extends JointBuilder(j) {
  j.body1 = bodies._1
  j.body2 = bodies._2
}

class GearJointBuilder(j: GearJointDef, joints: (Joint, Joint))
  extends JointBuilder(j) {
  j.joint1 = joints._1
  j.joint2 = joints._2
  def ratio(v: Float) = { j.ratio = v; this }
}

class DistanceJointBuilder(j: DistanceJointDef, bodies: (Body, Body))
  extends BodyJointBuilder(j, bodies) {
  def localAnchor1(v: Vector2) = { j.localAnchor1 = v; this }
  def localAnchor2(v: Vector2) = { j.localAnchor2 = v; this }
  def length(v: Float) = { j.length = v; this }
  def frequencyHz(v: Float) = { j.frequencyHz = v; this }
  def dampingRatio(v: Float) = { j.dampingRatio = v; this }
}

class RevoluteJointBuilder(j: RevoluteJointDef, bodies: (Body, Body))
  extends BodyJointBuilder(j, bodies) {
  def localAnchor1(v: Vector2) = { j.localAnchor1 = v; this }
  def localAnchor2(v: Vector2) = { j.localAnchor2 = v; this }
  def referenceAngle(v: Float) = { j.referenceAngle = v; this }
  def anchor(v: Vector2) = {
    j.localAnchor1 = j.body1.toLocalPoint(v)
    j.localAnchor2 = j.body2.toLocalPoint(v)
    j.referenceAngle = j.body2.angle - j.body1.angle
    this
  }
  def enableLimit(v: Boolean) = { j.enableLimit = v; this }
  def lowerAngle(v: Float) = { j.lowerAngle = v; this }
  def upperAngle(v: Float) = { j.upperAngle = v; this }
  def enableMotor(v: Boolean) = { j.enableMotor = v; this }
  def motorSpeed(v: Float) = { j.motorSpeed = v; this }
  def maxMotorTorque(v: Float) = { j.maxMotorTorque = v; this }
}

class PrismaticJointBuilder(j: PrismaticJointDef, bodies: (Body, Body))
  extends BodyJointBuilder(j, bodies) {
  def localAnchor1(v: Vector2) = { j.localAnchor1 = v; this }
  def localAnchor2(v: Vector2) = { j.localAnchor2 = v; this }
  def referenceAngle(v: Float) = { j.referenceAngle = v; this }
  def anchor(v: Vector2) = {
    j.localAnchor1 = j.body1.toLocalPoint(v)
    j.localAnchor2 = j.body2.toLocalPoint(v)
    j.referenceAngle = j.body2.angle - j.body1.angle
    this
  }
  def localAxis1(v: Vector2) = { j.localAxis1 = v; this }
  def axis(v: Vector2) = {
    j.localAxis1 = j.body1.toLocalVector(v);
    this
  }
  def enableLimit(v: Boolean) = { j.enableLimit = v; this }
  def lowerTranslation(v: Float) = { j.lowerTranslation = v; this }
  def upperTranslation(v: Float) = { j.upperTranslation = v; this }
  def enableMotor(v: Boolean) = { j.enableMotor = v; this }
  def motorSpeed(v: Float) = { j.motorSpeed = v; this }
  def maxMotorForce(v: Float) = { j.maxMotorForce = v; this }
}
