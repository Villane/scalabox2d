package org.villane.box2d.common

/**
 * Doubly linked list item base class. Not for clients.
 */
sealed trait DLLItemCommonBase[T >: Null] {
  def elem: T
  private[common] var prev: DLLItemCommonBase[T] = null
  private[common] var next: DLLItemCommonBase[T] = null
}

/**
 * Doubly linked list item base. This can be mixed in into a class whose
 * instances are all part of the same list.
 */
trait DLLItemBase[T >: Null] extends DLLItemCommonBase[T] { self: T =>
  def elem: T = this
}

/**
 * Doubly linked list item. This can be used as members of a class whose
 * instances need to be part of lists.
 */
final case class DLLItem[T >: Null](elem: T) extends DLLItemCommonBase[T]

class DLLHead[T >: Null](var head: DLLItemCommonBase[T]) extends Collection[T] {
  var itemCount = if (head == null) 0 else 1
  def size = itemCount

  def add(item: DLLItemCommonBase[T]) {
    item.prev = null
    item.next = head
    if (head != null) head.prev = item
    head = item
    itemCount += 1
  }

  def remove(item: DLLItemCommonBase[T]) {
    if (item.prev != null) item.prev.next = item.next
    if (item.next != null) item.next.prev = item.prev
    if (head == item) head = item.next
    itemCount -= 1
  }

  def clear() {
    head = null
  }

  def elements: Iterator[T] = new Iterator[T] {
    var nextItem = head
    def hasNext = nextItem != null
    def next = {
      val res = nextItem
      if (res != null) {
        nextItem = nextItem.next
        res.elem
      } else {
        throw new NoSuchElementException("no more elements")
      }
    }
  }
}
