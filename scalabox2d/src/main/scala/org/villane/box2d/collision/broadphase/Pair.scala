package org.villane.box2d.collision.broadphase

object Pair {
  val Buffered = 0x0001
  val Removed = 0x0002
  val Final = 0x0004
}

case class Pair(var proxyId1: Int, var proxyId2: Int) extends Ordered[Pair] {
  var userData: AnyRef = null
  var status = 0
  
  def setBuffered() = status |= Pair.Buffered
  def clearBuffered() = status &= ~Pair.Buffered
  def isBuffered = (status & Pair.Buffered) == Pair.Buffered

  def setRemoved() = status |= Pair.Removed
  def clearRemoved() = status &= ~Pair.Removed
  def isRemoved = (status & Pair.Removed) == Pair.Removed

  def setFinal() = status |= Pair.Final
  def isFinal = (status & Pair.Final) == Pair.Final

  def compare(p: Pair) = {
    val d = proxyId1 - p.proxyId1
    if (d != 0)
      d
    else
      proxyId2 - p.proxyId2
  }
}