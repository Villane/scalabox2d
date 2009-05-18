package org.villane.box2d.common

trait DoublyLinkedListItem[T >: Null <: DoublyLinkedListItem[T]] extends Iterable[T] { self: T =>
  var prev: T = null
  var next: T = null
  type Setter = (T => Unit)

  def add(headValue: T, setHead: Setter) {
    prev = null
    next = headValue
    if (headValue != null) {
      headValue.prev = this
    }
    setHead(this)
  }
  
  def remove(headValue: T, setHead: Setter) {
    if (prev != null) {
      prev.next = next
    }
    if (next != null) {
      next.prev = prev
    }
    if (headValue == this) {
      setHead(next)
    }
  }

  def elements: Iterator[T] = new Iterator[T] {
    var nextElem = self
    def hasNext = nextElem != null
    def next = {
      val res = nextElem
      if (nextElem != null) nextElem = nextElem.next
      if (res == null)
        throw new NoSuchElementException("no more elements")
      else
        res
    }
  }
}
