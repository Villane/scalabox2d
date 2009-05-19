package org.villane.box2d.dynamics.contacts

/**
 * Contact event type.
 * 
 * TODO perhaps move this to a preamble?
 */
object EventType {
  object Add extends EventType
  object Persist extends EventType
  object Remove extends EventType
}
sealed trait EventType
