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
      val f = if (invSqr && sensor.isDynamic)
        G / r2 / sqrt(r2) * body.mass * sensor.mass * d
      else if (sensor.isDynamic)
        G / r2 * body.mass * sensor.mass * 10 * d
      else
        G / r2 * body.mass * d
      body.applyForce(f, body.worldCenter)
    }
  }

}
