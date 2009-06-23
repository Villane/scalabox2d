package org.villane.box2d.svg

import vecmath._
import vecmath.Preamble._
import dsl._
import dsl.DSL._

import shapes._
import dynamics._

import ConvexHull._

import java.io.FileInputStream
import org.newdawn.slick.Color
import org.newdawn.slick.svg._
import org.newdawn.slick.geom

/**
 * TODO offset?
 */
class SlickSVGSceneLoader(fileName: String, scale: Float)
  extends SceneFactory {

  implicit def slickVector2fToVector2(v: geom.Vector2f) = Vector2(v.x, v.y)

  var gravity = Vector2(0, 9.81f)

  def create = {
    val svg = InkscapeLoader.load(new FileInputStream(fileName), false)
    val aabb = AABB(Vector2.Zero, (svg.getWidth, svg.getHeight) / scale)
    val world = new World(aabb, gravity, true)
    createScene(svg)(world)
    world
  }

  def createScene(svg: Diagram)(implicit world: World) {
    for (i <- 0 until svg.getFigureCount) {
      createFigure(svg.getFigure(i))
    }
  }

  def createFigure(fig: Figure)(implicit world: World) = fig.getType match {
    case Figure.ELLIPSE =>
      val c = fig.getShape.asInstanceOf[geom.Shape]
      body {
        pos((c.getCenterX, c.getCenterY) / scale)
        circle((c.getMinX - c.getCenterX).abs / scale) density 1
        if (isDynamic(fig)) massFromShapes
      }
    case Figure.LINE =>
      val line = fig.getShape.asInstanceOf[geom.Line]
      body {
        edge(line.getStart / scale, line.getEnd / scale)
      }
    case Figure.RECTANGLE =>
      val poly = fig.getShape.asInstanceOf[geom.Polygon]
      body {
        pos((poly.getCenterX, poly.getCenterY) / scale)
        box(
          (poly.getMinX - poly.getCenterX).abs / scale,
          (poly.getMinY - poly.getCenterY).abs / scale
        ) density 1
        if (isDynamic(fig)) massFromShapes
      }
    case Figure.PATH =>
      val path = fig.getShape.asInstanceOf[geom.Path]
      body {
        val ps = new Array[Vector2](path.getPointCount)
        for (i <- 0 until path.getPointCount) {
          val p = path.getPoint(i)
          ps(i) = (p(0), p(1)) / scale
        }
        edge(ps:_*)
      }
    case Figure.POLYGON =>
      val poly = fig.getShape.asInstanceOf[geom.Polygon]
      body {
        pos(poly.getCenterX / scale, poly.getCenterY / scale)
        val ps = new Array[Vector2](poly.getPointCount)
        for (i <- 0 until poly.getPointCount) {
          val p = poly.getPoint(i)
          ps(i) = (p(0) - poly.getCenterX, p(1) - poly.getCenterY) / scale
        }
        if (ps.length <= 8 && poly.closed) {
          polygon(convexHull(ps):_*) density 1
          if (isDynamic(fig)) massFromShapes
        } else {
          edge(ps:_*)
        }
      }
  }

  def isDynamic(fig: Figure) =
    fig.getData.getAttribute("fill") != null &&
      fig.getData.isColor("fill") &&
        Color.red == fig.getData.getAsColor("fill")

}
