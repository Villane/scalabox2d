package org.villane.box2d.draw

import vecmath._
import vecmath.Preamble._

object DrawFlags {
  /** draw shapes */
  val shape = 0x0001
  /** draw joint connections */
  val joint = 0x0002
  /** @deprecated draw core (TOI) shapes */
  val coreShape = 0x0004
  /** draw axis aligned bounding boxes */
  val aabb = 0x0008
  /** draw broad-phase pairs */
  val pair = 0x0020
  /** draw center of mass frame */
  val centerOfMass = 0x0040
}

/**
 * Implement this abstract class to allow JBox2d to automatically draw your
 * physics for debugging purposes. Not intended to replace your own custom
 * rendering routines!
 */
trait DebugDrawHandler {
  /// Implement and register this class with a b2World to provide debug drawing of physics
  /// entities in your game.
  var drawFlags = 0

  def appendFlags(flags: Int) = drawFlags |= flags
  def clearFlags(flags: Int) = drawFlags &= ~flags

  /// Draw a closed polygon provided in CCW order.
  def drawPolygon(vertices: Array[Vector2f], color: Color3f)

  /// Draw a solid closed polygon provided in CCW order.
  def drawSolidPolygon(vertices: Array[Vector2f], color: Color3f)

  /// Draw a circle.
  def drawCircle(center: Vector2f, radius: Float, color: Color3f)

  /// Draw a solid circle.
  def drawSolidCircle(center: Vector2f, radius: Float, axis: Vector2f, color: Color3f)

  /// Draw a point.
  def drawPoint(position: Vector2f, f: Float, color: Color3f)

  /// Draw a line segment.
  def drawSegment(p1: Vector2f, p2: Vector2f, color: Color3f)

  /// Draw a transform. Choose your own length scale.
  /// @param xf a transform.
  def drawTransform(xf: Transform2f)

  def drawString(x: Float, y: Float, s: String, color: Color3f)

  //All the following should be overridden if the concrete drawing
  //class does any sort of camera movement

  /**
   * Stub method to overload for camera movement/zoom.
   * @param x - x coordinate of camera
   * @param y - y coordinate of camera
   * @param scale - zoom factor
   */
  def setCamera(x: Float, y: Float, scale: Float) {}

  /**
   * @param screenV Screen position
   * @return World position
   */
  def screenToWorld(screenV: Vector2f) = screenV

  /**
   * @param screenx Screen x position
   * @param screeny Screey y position
   * @return World position
   */
  final def screenToWorld(screenX: Float, screenY: Float): Vector2f = screenToWorld(Vector2f(screenX, screenY))

  /**
   * @param worldV World position
   * @return Screen position
   */
  def worldToScreen(worldV: Vector2f) = worldV

  /**
   * @param worldx World x position
   * @param worldy World y position
   * @return Screen position
   */
  final def worldToScreen(worldX: Float, worldY: Float): Vector2f = worldToScreen(Vector2f(worldX, worldY))

}
