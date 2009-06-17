package org.villane.box2d.dynamics.controllers

import vecmath._
import vecmath.Preamble._

trait Controller {

  var bodies: List[Body] = Nil

  def step(step: TimeStep)

  protected def forAwakeBodies(f: Body => Unit) =
    for (body <- bodies) if (!body.isSleeping) f(body)

}
