package org.villane.box2d.testbed.scenes

import vecmath._
import vecmath.Preamble._
import shapes._
import dynamics._
import dsl._

trait TestbedScene extends SceneFactory {
  final def create = {
    val world = createWorld
    createScene(world)
    world
  }

  def createScene(implicit world: World)

  protected def createWorld = new World(
    AABB((-200, -100), (200, 200)),
    (0, -10),
    true
  )
}
