package org.villane.box2d.dynamics.contacts

import collision.Manifold

// This lets us provide broadphase proxy pair user data for
// contacts that shouldn't exist.
object NullContact extends Contact(null,null) {
  def evaluate(listener: ContactListener) {}
  val manifolds = Array[Manifold]()
}
