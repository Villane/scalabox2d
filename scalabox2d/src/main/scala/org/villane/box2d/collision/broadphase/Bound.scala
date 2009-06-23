package org.villane.box2d.collision.broadphase

case class Bound(
  var value: Int,
  var proxyId: Int
) {

  def this(b: Bound) = {
    this(b.value, b.proxyId)
    stabbingCount = b.stabbingCount
  }

  var stabbingCount = 0

  def isLower = (value & 1) == 0

  def isUpper = (value & 1) == 1

  override def toString = {
    var ret = "Bound variable:\n";
    ret += "value: " + value + "\n";
    ret += "proxyId: " + proxyId + "\n";
    ret += "stabbing count: " + stabbingCount + "\n";
    ret
  }

}
