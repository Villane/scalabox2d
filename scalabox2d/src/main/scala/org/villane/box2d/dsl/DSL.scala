package org.villane.box2d.dsl

import vecmath._
import vecmath.Preamble._
import shapes._
import collision._
import dynamics._
import collection.mutable.ListBuffer

object DSL {
  object BodyCtx extends ThreadLocal[BodyCtx]
  class BodyCtx {
    var massFromShapes = false
    val bodyBuilder = new BodyBuilder(new BodyDef)
    val fixBuilders = new ListBuffer[FixtureBuilder]
  }

  def body[T](block: => T)(implicit world: World) {
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
  def circle(radius: Float) = fbuilder(CircleDef(Vector2f.Zero, radius))
  def circle(pos: Vector2f, radius: Float) = fbuilder(CircleDef(pos, radius))
  def polygon(vertices: Vector2f*) = fbuilder(PolygonDef(vertices.toArray))
  def box(halfW: Float, halfH: Float) = fbuilder(PolygonDef.box(halfW, halfH))
  def box(halfW: Float, halfH: Float, center: Vector2f) =
    fbuilder(PolygonDef.box(halfW, halfH, center))

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
}

class BodyBuilder(b: BodyDef) {
  def userData(userData: AnyRef) = { b.userData = userData; this }
  def mass(mass: Mass) = { b.mass = mass; this }
  def pos(pos: Vector2f) = { b.pos = pos; this } 
  def angle(angle: Float) = { b.angle = angle; this } 
  def linearDamping(linearDamping: Float) = { b.linearDamping = linearDamping; this }
  def angularDamping(angularDamping: Float) = { b.angularDamping = angularDamping; this }
  def sleepingAllowed(allowSleep: Boolean) = { b.allowSleep = allowSleep; this }
  def initiallySleeping(isSleeping: Boolean) = { b.isSleeping = isSleeping; this }
  def fixedRotation(fixedRotation: Boolean) = { b.fixedRotation = fixedRotation; this }
  def bullet(isBullet: Boolean) = { b.isBullet = isBullet; this }
  def define = b
}

class FixtureBuilder(s: FixtureDef) {
  def userData(userData: AnyRef) = { s.userData = userData; this }
  def friction(friction: Float) = { s.friction = friction; this }
  def restitution(restitution: Float) = { s.restitution = restitution; this }
  def density(density: Float) = { s.density = density; this }
  def filter(filter: FilterData) = { s.filter = filter; this }
  def sensor(isSensor: Boolean) = { s.isSensor = isSensor; this }
  def define = s
}
