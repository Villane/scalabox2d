package org.villane.box2d.testbed

import box2d.collision.ContactID;
import box2d.shapes.Shape;
import vecmath.Vector2f;

/**
 * Holder for storing contact information.
 */
class ExampleContactPoint {
  var shape1: Shape = null
  var shape2: Shape = null
  var normal = Vector2f.Zero
  var position = Vector2f.Zero
  var velocity = Vector2f.Zero
  var id = ContactID.Zero
  var state = 0 // 0-add, 1-persist, 2-remove
}