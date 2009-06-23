package org.villane.box2d.dynamics.controllers

import vecmath.Preamble._
import dsl.DSL._

class PlanetGravityController extends Controller with SensorManagedBodies {
  /** Specifies the strength of the gravitiation force */
  var G = 0f

  /** If true, gravity is proportional to r^-2, otherwise r^-1 */
  var invSqr = false

  def createSensor(body: Body, radius: Float) = {
    body.createFixture(circle(radius) sensor true)
    sensor = body
  }

  def step(step: TimeStep) = forAwakeBodies { body =>
    val d = sensor.worldCenter - body.worldCenter
    val r2 = d.lengthSquared
    if (r2 >= Settings.Epsilon) {
      val sm = if (sensor.isDynamic) sensor.mass else 1f
      val f = if (invSqr)
        G / r2 / sqrt(r2) * body.mass * sm * d
      else
        G / r2 * body.mass * sm * d
      body.applyForce(f, body.worldCenter)
    }
  }

}
