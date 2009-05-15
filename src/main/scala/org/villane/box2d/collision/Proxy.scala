package org.villane.box2d.collision

class Proxy {
  val lowerBounds = Array(0,0)
  val upperBounds = Array(0,0)
  var overlapCount = BroadPhase.Invalid
  var timeStamp = 0
  var categoryBits = 0
  var maskBits = 0
  var groupIndex = 0
  var userData: AnyRef = null

  def next = lowerBounds(0)
  def next_=(next: Int) = lowerBounds(0) = next
 
  def isValid = overlapCount != BroadPhase.Invalid
}
