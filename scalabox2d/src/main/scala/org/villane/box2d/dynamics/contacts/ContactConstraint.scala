package org.villane.box2d.dynamics.contacts

import vecmath.Vector2f
import collision.Manifold
import dynamics.Body

class ContactConstraint {
  var points: Array[ContactConstraintPoint] = null
  var normal = Vector2f.Zero
  var manifold: Manifold = null
  var body1: Body = null
  var body2: Body = null
  var friction = 0f
  var restitution = 0f
  /*var pointCount = 0
  points = new ContactConstraintPoint[Settings.maxManifoldPoints];
  for (int i = 0; i < Settings.maxManifoldPoints; i++) {
            points[i] = new ContactConstraintPoint();
        }
  */
}
