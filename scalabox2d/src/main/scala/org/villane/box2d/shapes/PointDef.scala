package org.villane.box2d.shapes

import vecmath._

object PointDef {
  def apply(pos: Vector2f, mass: Float) = {
    val d = new PointDef
    d.pos = pos
    d.mass = mass
    d
  }
}

class PointDef extends ShapeDef {
  var pos = Vector2f.Zero
  var mass = 0f
}
