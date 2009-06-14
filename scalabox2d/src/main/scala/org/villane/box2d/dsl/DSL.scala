package org.villane.box2d.dsl

import vecmath._
import vecmath.Preamble._
import shapes._
import collision._
import dynamics._
import dynamics.joints._
import collection.mutable.ListBuffer

object DSL {
  object BodyCtx extends ThreadLocal[BodyCtx]
  class BodyCtx {
    var massFromShapes = false
    val bodyBuilder = new BodyBuilder(new BodyDef)
    val fixBuilders = new ListBuffer[FixtureBuilder]
  }

  def body[T](block: => T)(implicit world: World) = {
    val ctx = new BodyCtx
    BodyCtx.set(ctx)
    block
    val body = world.createBody(ctx.bodyBuilder.define)
    for (builder <- ctx.fixBuilders) {
      val fixDef = builder.define
      body.createFixture(fixDef)
    }
    if (ctx.massFromShapes) body.computeMassFromShapes
    BodyCtx.remove
    body
  }

  def fixture(fd: FixtureDef) = {
    val builder = new FixtureBuilder(fd)
    if (BodyCtx.get != null) BodyCtx.get.fixBuilders += builder
    builder
  }

  def fixture(fb: FixtureBuilder) = {
    if (BodyCtx.get != null) BodyCtx.get.fixBuilders += fb
    fb
  }

  private def fbuilder(sd: ShapeDef) = {
    val builder = new FixtureBuilder(new FixtureDef(sd))
    if (BodyCtx.get != null) BodyCtx.get.fixBuilders += builder
    builder
  }

  def shape(shapeDef: ShapeDef) = fbuilder(shapeDef)
  def circle(radius: Scalar) = fbuilder(CircleDef(Vector2.Zero, radius))
  def circle(pos: Vector2, radius: Scalar) = fbuilder(CircleDef(pos, radius))
  def polygon(vertices: Vector2*) = fbuilder(PolygonDef(vertices.toArray))
  def box(halfW: Scalar, halfH: Scalar) = fbuilder(PolygonDef.box(halfW, halfH))
  def box(halfW: Scalar, halfH: Scalar, center: Vector2) =
    fbuilder(PolygonDef.box(halfW, halfH, center))
  def box(halfW: Scalar, halfH: Scalar, center: Vector2, angle: Scalar) =
    fbuilder(PolygonDef.box(halfW, halfH, center, angle))

  private def bbuilder = BodyCtx.get.bodyBuilder

  def userData = bbuilder.userData _
  def mass = bbuilder.mass _
  def pos = bbuilder.pos _
  def angle = bbuilder.angle _
  def linearDamping = bbuilder.linearDamping _
  def angularDamping = bbuilder.angularDamping _
  def sleepingAllowed = bbuilder.sleepingAllowed _
  def initiallySleeping = bbuilder.initiallySleeping _
  def fixedRotation = bbuilder.fixedRotation _
  def bullet = bbuilder.bullet _

  def massFromShapes = BodyCtx.get.massFromShapes = true

  def joint[JB <: JointBuilder](jBuilder: => JB)(implicit world: World) = {
    val jDef = jBuilder.define
    world.createJoint(jDef)
  }

  def revolute(bodies: (Body, Body)) = new RevoluteJointBuilder(
    new RevoluteJointDef, bodies)
  def distance(bodies: (Body, Body)) = new DistanceJointBuilder(
    new DistanceJointDef, bodies)
}

class BodyBuilder(b: BodyDef) {
  def userData(userData: AnyRef) = { b.userData = userData; this }
  def mass(mass: Mass) = { b.mass = mass; this }
  def pos(pos: Vector2) = { b.pos = pos; this } 
  def angle(angle: Scalar) = { b.angle = angle; this } 
  def linearDamping(linearDamping: Scalar) = { b.linearDamping = linearDamping; this }
  def angularDamping(angularDamping: Scalar) = { b.angularDamping = angularDamping; this }
  def sleepingAllowed(allowSleep: Boolean) = { b.allowSleep = allowSleep; this }
  def initiallySleeping(isSleeping: Boolean) = { b.isSleeping = isSleeping; this }
  def fixedRotation(fixedRotation: Boolean) = { b.fixedRotation = fixedRotation; this }
  def bullet(isBullet: Boolean) = { b.isBullet = isBullet; this }
  def define = b
}

class FixtureBuilder(s: FixtureDef) {
  def userData(userData: AnyRef) = { s.userData = userData; this }
  def friction(friction: Scalar) = { s.friction = friction; this }
  def restitution(restitution: Scalar) = { s.restitution = restitution; this }
  def density(density: Scalar) = { s.density = density; this }
  def filter(filter: FilterData) = { s.filter = filter; this }
  def sensor(isSensor: Boolean) = { s.isSensor = isSensor; this }
  def define = s
}
