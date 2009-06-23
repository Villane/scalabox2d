/* TODO HEADER */
package org.villane.box2d.collision

object ContactID {
  val Zero = ContactID(0, 0, 0, false)
  val NullFeature = Int.MaxValue
}

/**
 * Contact ids to facilitate warm starting.
 * The features that intersect to form the contact point
 */
case class ContactID(
  /** The edge that defines the outward contact normal. */
  referenceEdge: Int,
  /** The edge most anti-parallel to the reference edge. */
  incidentEdge: Int,
  /** The vertex (0 or 1) on the incident edge that was clipped. */
  incidentVertex: Int,
  /** True indicates that the reference edge is on shape2. */
  flip: Boolean
) {
  /** Returns a new ContactID based on this one, but with the features.flip value changed. */
  def withFlip(flip: Boolean) = ContactID(
    this.referenceEdge,
    this.incidentEdge,
    this.incidentVertex,
    flip
  )
}
