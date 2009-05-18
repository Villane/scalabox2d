package org.villane.box2d.shapes

import vecmath.Vector2f

object CircleDef {
  def apply(pos: Vector2f, radius: Float) = {
    val cd = new CircleDef
    cd.pos = pos
    cd.radius = radius
    cd
  }
}

/** 
 * A circle shape definition. Set the local position and radius ((0,0) and 1 by default)
 * and then call Body.createShape(ShapeDef) to add the shape to a body.
 */
class CircleDef extends ShapeDef {
  var pos = Vector2f.Zero
  var radius = 1f
}
