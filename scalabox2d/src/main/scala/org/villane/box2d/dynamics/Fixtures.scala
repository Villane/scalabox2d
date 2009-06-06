package org.villane.box2d.dynamics

import vecmath._
import vecmath.Preamble._
import shapes._

object Fixtures {
  def circle(pos: Vector2f, radius: Float) = new FixtureBuilder(new FixtureDef(
    CircleDef(pos, radius)))
  def polygon(vertices: Vector2f*) = new FixtureBuilder(new FixtureDef(
    PolygonDef(vertices:_*)))
  def box(halfW: Float, halfH: Float) = new FixtureBuilder(new FixtureDef(
    PolygonDef.box(halfW, halfH)))
  def box(halfW: Float, halfH: Float, center: Vector2f) = new FixtureBuilder(new FixtureDef(
    PolygonDef.box(halfW, halfH, center)))
}

class FixtureBuilder(s: FixtureDef) {
  def withUserData(userData: AnyRef) = { s.userData = userData; this }
  def withFriction(friction: Float) = { s.friction = friction; this }
  def withRestitution(restitution: Float) = { s.restitution = restitution; this }
  def withDensity(density: Float) = { s.density = density; this }
  def withFilter(filter: FilterData) = { s.filter = filter; this }
  def asSensor(isSensor: Boolean) = { s.isSensor = isSensor; this }
  def define = s
}
