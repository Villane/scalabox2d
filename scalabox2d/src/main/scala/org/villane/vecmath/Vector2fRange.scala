package org.villane.vecmath

class Vector2fRange(start: Vector2f, end: Vector2f, step: Vector2f)
  extends Iterable[Vector2f] {

  def elements = new Iterator[Vector2f] {
    var v = start
    def hasNext = inInterval(v)
    def next = {
      var n = v
      v += step
      n
    }
  }

  def by(step: Vector2f) = new Vector2fRange(start, end, step)

  /*override def foreach(f: Vector2f => Unit) {
    var i = this.start
    val until = if (inInterval(end)) end + 1 else end

    while (i.x < until.x || i.y < until.y) {
      f(i)
      i += step
    }
  }*/

  /** Is the argument inside the interval defined by `start' and `end'? 
   *  Returns true if `v' is inside [start, end).
   */
  protected def inInterval(v: Vector2f): Boolean =
    (v.x >= start.x && v.x < end.x) || 
    (v.y >= start.y && v.y < end.y)
}
