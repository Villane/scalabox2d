package org.villane.box2d.dynamics

import java.util.concurrent._

object IslandSolverWorker {
  // Single work queue
  val workQueue = new LinkedBlockingQueue[FutureTask[Int]]

  var workers: List[IslandSolverWorker] = Nil
  for (i <- 1 until Settings.numThreads) {
    val worker = new IslandSolverWorker()
    workers ::= worker
    worker.start
  }
  def stopWorkers = workers foreach { w => w.shouldStop = true; w.interrupt }
}

class IslandSolverWorker extends Thread {
  var shouldStop = false
  override def run = while (!shouldStop) IslandSolverWorker.workQueue.take.run
}
