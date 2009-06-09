package org.villane.vecmath

class Vector2fTimes(start: Vector2f, times: Int, step: Vector2f)
  extends Iterable[Vector2f] {

  def elements = new Iterator[Vector2f] {
    var v = start
    var n = times
    def hasNext = n >= 0
    def next = {
      var ov = v
      v += step
      n -= 1
      ov
    }
  }

  def by(step: Vector2f) = new Vector2fTimes(start, times, step)
}
