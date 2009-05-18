package org.villane.box2d.common

abstract class LookupTable[T](val precision: Int) {
  private[this] val values = new Array[T](precision)
  def apply(arg: T) = values(toIndex(arg)) 
  def toIndex(arg: T): Int
  def toValue(i: Int): T
  def fill() {
    for (i <- 0 until precision) {
      values(i) = toValue(i)
    }
  } 
}

// Example usage of lookup table
class SinTable(val size: Int) extends LookupTable[Float](size) {
  val TwoPi = Math.Pi.toFloat * 2
  val rad_slice = TwoPi / size
  fill
  def toValue(i: Int) = Math.sin(i * rad_slice).toFloat
  def toIndex(radians: Float) = ((radians / TwoPi) * size).toInt & (size - 1)
}
// Example usage of lookup table
class CosTable(val size: Int) extends LookupTable[Float](size) {
  val TwoPi = Math.Pi.toFloat * 2
  val rad_slice = TwoPi / size
  fill
  def toValue(i: Int) = Math.cos(i * rad_slice).toFloat
  def toIndex(radians: Float) = ((radians / TwoPi) * size).toInt & (size - 1)
}
