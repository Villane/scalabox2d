package org.villane.box2d.testbed

import vecmath._
import dynamics._
import dsl._

class HeadlessPerformanceTest(sceneFactory: SceneFactory) {

  val settings = new TestSettings

  def run: Unit = run(false, 500, 0, false)

  def run(warmUp: Boolean, steps: Int, split: Int, waitForKeypress: Boolean) {
    //Settings.threadedIslandSolving = true
    //Settings.numThreads = 3
    if (warmUp) {
      val world = sceneFactory.create
      var i = 0
      while (i < steps) { step(world); i += 1 }
    }

    val world = sceneFactory.create
    Vector2.creationCount = 0
    //var x = 0
    //m_world.bodyList foreach { b => if (!b.isStatic) x += 1 }
    //println("bodycount: " + x)
    if (waitForKeypress && split == 0) {
      println("Attach profiler and press Enter to start test: ")
      Console.readLine
    }
    var start = System.currentTimeMillis
    var i = 0
    var splitTime = 0L
    while (i < steps) {
      if (i == split && split > 0) {
        if (waitForKeypress) {
          println("Attach profiler and press Enter to start test: ")
          Console.readLine
        }
        splitTime = System.currentTimeMillis
      }
      step(world)
      i += 1
    }
    /*while (m_world.bodyList exists { b =>
      !b.isSleeping && !b.isStatic
    }) {
      step
    }*/
    val end = System.currentTimeMillis
    println((end - start) + " ms")
    if (split > 0) {
      println(" first " + split + ": " + (splitTime - start) + " ms")
      println(" last  " + (steps - split) + ": " + (end - splitTime) + " ms")
    }
    println("vectors created:" + Vector2.creationCount)
    IslandSolverWorker.stopWorkers
    util.Timing.printCollectedTimes
  }

  def step(world: World) {
    var timeStep = if (settings.hz > 0.0f) 1.0f / settings.hz else 0.0f

    world.warmStarting = settings.enableWarmStarting
    world.positionCorrection = settings.enablePositionCorrection
    world.continuousPhysics = settings.enableTOI

    world.step(timeStep, settings.iterationCount)
  }

}
