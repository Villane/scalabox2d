package org.villane.box2d.dynamics

import vecmath._
import shapes.Mass

object Bodies {
  def body = new BodyBuilder(new BodyDef)
}

class BodyBuilder(b: BodyDef) {
  def withUserData(userData: AnyRef) = { b.userData = userData; this }
  def withMass(mass: Mass) = { b.mass = mass; this }
  def withPos(pos: Vector2f) = { b.pos = pos; this } 
  def withAngle(angle: Float) = { b.angle = angle; this } 
  def withLinearDamping(linearDamping: Float) = { b.linearDamping = linearDamping; this }
  def withAngularDamping(angularDamping: Float) = { b.angularDamping = angularDamping; this }
  def withSleepingAllowed(allowSleep: Boolean) = { b.allowSleep = allowSleep; this }
  def initiallySleeping(isSleeping: Boolean) = { b.isSleeping = isSleeping; this }
  def withFixedRotation(fixedRotation: Boolean) = { b.fixedRotation = fixedRotation; this }
  def asBullet(isBullet: Boolean) = { b.isBullet = isBullet; this }
  def define = b
}
