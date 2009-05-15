package org.villane.box2d.common

object Timing {
  def currentTime = scala.compat.Platform.currentTime
  def nanoTime = System.nanoTime
  
  lazy val collectedTimes = new collection.mutable.HashMap[String, Long]
  
  def repeat(times: Long)(block: => Unit) = {
    assert(times >= 0)
    var t = times;
    while(t > 0) {
      block
      t -= 1
    }
  }
  
  def repeatWithIndex(times: Long)(block: Long => Unit) = {
    assert(times >= 0)
    var t = 0;
    while(t < times) {
      block(t)
      t += 1
    }
  }
  
  def time[T](name: String)(block: => T) = {
    val start = currentTime
    try {
        block
    } finally {
        val diff = currentTime - start
        println("# Block \"" + name +"\" completed, time taken: " + diff + " ms (" + diff / 1000.0 + " s)")
    }
  }

  def timeNano[T](name: String)(block: => T) = {
    val start = nanoTime
    try {
        block
    } finally {
        val diff = nanoTime - start
        println("# Block \"" + name +"\" completed, time taken: " + diff + " ns (" + diff / 1000000.0 + " ms)")
    }
  }

  def collectNanoTime[T](name: String)(block: => T) = {
    val start = nanoTime
    try {
        block
    } finally {
        val diff = nanoTime - start
        val old = collectedTimes.getOrElseUpdate(name, 0)
        collectedTimes(name) = old + diff
    }
  }

  def printCollectedTimes() {
    for((name, time) <- collectedTimes) {
      println("# Blocks \"" + name +"\" completed, time taken: " + time + " ns (" + time / 1000000.0 + " ms)")
    }
    collectedTimes.clear
  }
}
