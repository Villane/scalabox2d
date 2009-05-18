package org.villane.box2d.common

trait DoublyLinkedListItem[T >: Null <: DoublyLinkedListItem[T]] { self: T =>
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
    var a = self
    def hasNext = a.next != null
    def next = {
      a = a.next
      if (a == null)
        throw new NoSuchElementException("no more elements")
      else
        a
    }
  }
  
  def foreach(f: T => Unit): Unit = elements.foreach(f)
}
