package org.villane.box2d.draw

import vecmath._
import vecmath.Preamble._

/**
 * Implement this abstract class to allow JBox2d to automatically draw your
 * physics for debugging purposes. Not intended to replace your own custom
 * rendering routines!
 */
trait DebugDrawHandler {
  /// Draw a closed polygon provided in CCW order.
  def drawPolygon(vertices: Array[Vector2], color: Color3f)

  /// Draw a solid closed polygon provided in CCW order.
  def drawSolidPolygon(vertices: Array[Vector2], color: Color3f)

  /// Draw a circle.
  def drawCircle(center: Vector2, radius: Float, color: Color3f)

  /// Draw a solid circle.
  def drawSolidCircle(center: Vector2, radius: Float, axis: Vector2, color: Color3f)

  /// Draw a point.
  def drawPoint(position: Vector2, f: Float, color: Color3f)

  /// Draw a line segment.
  def drawSegment(p1: Vector2, p2: Vector2, color: Color3f)

  /// Draw a transform. Choose your own length scale.
  /// @param xf a transform.
  def drawTransform(xf: Transform2)

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
  def screenToWorld(screenV: Vector2) = screenV

  /**
   * @param screenx Screen x position
   * @param screeny Screey y position
   * @return World position
   */
  final def screenToWorld(screenX: Float, screenY: Float): Vector2 = screenToWorld(Vector2(screenX, screenY))

  /**
   * @param worldV World position
   * @return Screen position
   */
  def worldToScreen(worldV: Vector2) = worldV

  /**
   * @param worldx World x position
   * @param worldy World y position
   * @return Screen position
   */
  final def worldToScreen(worldX: Float, worldY: Float): Vector2 = worldToScreen(Vector2(worldX, worldY))

}
