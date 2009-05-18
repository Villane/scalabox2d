package org.villane.box2d.shapes

object FilterData {
  val Default = FilterData(0x0001, 0xFFFF, 0)
}

/** This holds contact filtering data. */
case class FilterData(
  /** The collision category bits. Normally you would just set one bit. */
  categoryBits: Int,
  /**
   * The collision mask bits. This states the categories that this
   * shape would accept for collision.
   */
  maskBits: Int,
  /**
   * Collision groups allow a certain group of objects to never collide (negative)
   * or always collide (positive). Zero means no collision group. Non-zero group
   * filtering always wins against the mask bits.
   */
  groupIndex: Int
)