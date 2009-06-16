package org.villane.box2d.dsl

import dynamics.World

trait SceneFactory {
  def create: World
}
