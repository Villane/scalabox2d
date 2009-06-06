package org.villane.box2d.shapes

import vecmath._

object EdgeDef {
  def apply(loop: Boolean, vertices: Vector2f*) = {
    val d = new EdgeDef
    d.loop = loop
    d.vertices = vertices.toArray
    d
  }
  def apply(vertices: Vector2f*): EdgeDef = apply(false, vertices:_*)
  def loop(vertices: Vector2f*): EdgeDef = apply(true, vertices:_*)
}

class EdgeDef extends ShapeDef {
  /** Whether to create an extra edge between the first and last vertices. */
  var loop = false
  /** The vertices in local coordinates. */
  var vertices: Array[Vector2f] = Array()
}
