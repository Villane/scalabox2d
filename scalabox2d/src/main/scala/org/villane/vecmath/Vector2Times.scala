package org.villane.vecmath

/**
 * TODO inclusive / exclusive
 */
class Vector2Times(start: Vector2, times: Int, step: Vector2)
  extends Iterable[Vector2] {

  def elements = new Iterator[Vector2] {
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

  def by(step: Vector2) = new Vector2Times(start, times, step)
}
