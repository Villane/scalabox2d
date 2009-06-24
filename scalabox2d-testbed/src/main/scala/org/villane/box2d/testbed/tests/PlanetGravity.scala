package org.villane.box2d.testbed.tests

import vecmath._
import vecmath.Preamble._

class PlanetGravity(_parent: TestbedMain) extends AbstractExample(_parent) {
  var firstTime = true
  val name = "Planet Gravity"

  val scene = scenes.PlanetGravity

  override def postStep() {
    /*if (newKeyDown(0x24)) { // J
      
    }
    val force = 3f
    if (keyDown(0xCB)) { // Left
      if (!scene.player.contactList.isEmpty) {
        val c = scene.player.contactList.first.contact
        if (!c.manifolds.isEmpty) {
          val m = c.manifolds.first
          if (m.points.size == 2) {
            val f = force * m.normal.normal
            scene.player.applyForce(f, scene.player.worldCenter)
          }
        }
      }
    }
    if (keyDown(0xCD)) { // Right
      if (!scene.player.contactList.isEmpty) {
        val c = scene.player.contactList.first.contact
        if (!c.manifolds.isEmpty) {
          val m = c.manifolds.first
          val f = -force * m.normal.normal
          scene.player.applyForce(f, scene.player.worldCenter)
        }
      }
    }*/
  }

  def create = {
    if (firstTime) {
      setCamera(0f, 0f, 15f)
      firstTime = false
    }

    scenes.PlanetGravity.create
  }

}
