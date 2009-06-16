package org.villane.box2d.svg

import vecmath._
import vecmath.Preamble._
import shapes._
import dynamics._
import scala.xml.XML
import dsl.DSL._

class PureScalaSVGParser(fileName: String, scale: Float, gravity: Vector2) {
  implicit def stringExt(s: String) = new {
    def toScalar = s.toFloat / scale
  }

  def parse = {
    val svg = XML.load("C:/drawing.svg")
    val aabb = {
      val width = (svg \ "@width").text.toScalar
      val height = (svg \ "@width").text.toScalar
      AABB(Vector2.Zero, (width, height))
    }
    implicit val world = new World(aabb, gravity, true)
    for(g <- svg \\ "g") {
      for (r <- g \ "rect") body {
        val width = (r \ "@width").text.toScalar
        val height = (r \ "@height").text.toScalar
        pos((r \ "@x").text.toScalar + width / 2,
            (r \ "@y").text.toScalar + height / 2)
        box(width / 2, height / 2)
      }
      for (path <- g \ "path") {
        val sp = "@{http://sodipodi.sourceforge.net/DTD/sodipodi-0.dtd}" + _

        if ("arc" == (path \ sp("type")).text) {
          val rx = (path \ sp("rx")).text.toScalar
          val ry = (path \ sp("ry")).text.toScalar
          if (rx == ry) body {
            val cx = (path \ sp("cx")).text.toScalar
            val cy = (path \ sp("cy")).text.toScalar
            pos(cx, cy)
            circle(rx) density 1
            massFromShapes
          }
        }
      }
    }
    world
  }
}
