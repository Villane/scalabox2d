package org.villane.box2d.dynamics.controllers

import vecmath._
import vecmath.Preamble._

import collection.Set

trait Controller {

  def bodies: Set[Body]

  def step(step: TimeStep)

  protected def forAwakeBodies(f: Body => Unit) =
    for (body <- bodies) if (!body.isSleeping) f(body)

}

trait SensorManagedBodies extends Controller {
  var sensor: Body = null
  def bodies: Set[Body] = Set() ++ 
    sensor.contactList filter {
      // Filter edges with no actual contacts
      _.contact.manifolds.size > 0
    } map { _.other }
}

trait SelfManagedBodies extends Controller {

  private[this] val _bodies = collection.mutable.Set[Body]()

  def bodies: Set[Body] = _bodies

  def addBody(body: Body) = {
    _bodies += body
  }

  def removeBody(body: Body) = {
    _bodies -= body
  }

}