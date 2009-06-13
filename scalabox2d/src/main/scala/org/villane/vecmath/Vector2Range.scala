package org.villane.vecmath

/**
 * TODO inclusive / exclusive, negative step
 */
class Vector2Range(start: Vector2, end: Vector2, step: Vector2)
  extends Iterable[Vector2] {

  def elements = new Iterator[Vector2] {
    var v = start
    def hasNext = inInterval(v)
    def next = {
      var n = v
      v += step
      n
    }
  }

  def by(step: Vector2) = new Vector2Range(start, end, step)

  /*override def foreach(f: Vector2 => Unit) {
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
  protected def inInterval(v: Vector2): Boolean =
    (v.x >= start.x && v.x < end.x) || 
    (v.y >= start.y && v.y < end.y)
}
