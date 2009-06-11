package org.villane.box2d

import vecmath._
import vecmath.Preamble._
import box2d.draw._
import box2d.shapes._
import box2d.collision._
import box2d.dynamics._
import box2d.dynamics.joints._
import box2d.dynamics.contacts.ContactListener
import box2d.testbed.TestSettings
import dsl.DSL._

object PyramidPerfTest {
  implicit var m_world: World = null
  var m_worldAABB: AABB = null
  val settings = new TestSettings

  def main(args: Array[String]) {
    Settings.threadedIslandSolving = false
    createWorld
    create
    var i = 0
    while (i < 1000) { step; i += 1 }
    createWorld
    create
    //var x = 0
    //m_world.bodyList foreach { b => if (!b.isStatic) x += 1 }
    //println("bodycount: " + x)
    //println("press enter")
    Console.readLine
    var start = System.currentTimeMillis
    i = 0
    var s100 = 0L
    while (i < 1000) {
      if (i == 99) s100 = System.currentTimeMillis
      step
      i += 1
    }
    /*while (m_world.bodyList exists { b =>
      !b.isSleeping && !b.isStatic
    }) {
      step
    }*/
    val end = System.currentTimeMillis
    println((end - start) + " ms")
    println(" first 100: " + (s100 - start) + " ms")
    println(" last  900: " + (end - s100) + " ms")
    println("vectors created:" + Vector2f.creationCount)
    //IslandSolverWorker.stopWorkers
  }

  def createWorld() {
      m_world = new World(AABB((-200,-100),(200,200)),
                          (0, -10f),
                          true)
      m_worldAABB = m_world.aabb
	}

  def create() {
    body {
      pos(0, -10)
      box(50, 10)
    }

    val box1 = box(0.5f, 0.5f) density 5f restitution 0f friction 0.9f

    var x = Vector2f(-10.0f, 0.75f)
    var y = Vector2f.Zero
    val Δx = Vector2f(0.5625f, 2.0f)
    val Δy = Vector2f(1.125f, 0.0f)

    for (i <- 0 until 25) {
      y = x
      for (j <- i until 25) body {
        pos(y)
        fixture(box1)
        massFromShapes
        y += Δy
      }
      x += Δx
    }
  }
	
	def step() {
		var timeStep = if (settings.hz > 0.0f) 1.0f / settings.hz else 0.0f
		
		if (settings.pause) {
			if (settings.singleStep) {
				settings.singleStep = false;
			} else {
				timeStep = 0.0f;
			}
		}

		m_world.warmStarting = settings.enableWarmStarting
		m_world.positionCorrection = settings.enablePositionCorrection
		m_world.continuousPhysics = settings.enableTOI

		m_world.step(timeStep, settings.iterationCount);
	}
}
