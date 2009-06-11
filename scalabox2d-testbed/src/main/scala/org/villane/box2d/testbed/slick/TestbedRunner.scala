package org.villane.box2d.testbed.slick

import vecmath._
import vecmath.Preamble._
import box2d.draw._
import box2d.shapes._
import box2d.collision._
import box2d.dynamics._
import box2d.dynamics.joints._
import box2d.dynamics.contacts.ContactListener
import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.BasicGame;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.tests.AntiAliasTest;
import box2d.testbed._

import fenggui.FengWrapper
import org.fenggui.event.mouse.MouseButton
import org.fenggui.binding.render.lwjgl.EventHelper

object TestbedRunner {
  def main(args: Array[String]) {
    val container = new AppGameContainer(new SlickTestGame())
    container.setDisplayMode(800,600,false)
    container.start()
  }
}

/**
 * A bare-bones Slick implementation of the Pyramid demo.
 * Just enough to get to the drawing - no input handling
 * of any sort, and only very basic camera handling.
 * <BR><BR>
 * Should get you on your way, though.
 * 
 * @author ewjordan
 *
 */
class SlickTestGame extends BasicGame("Slick/JBox2d Testbed (Scala)") with TestbedMain with FengWrapper {

  val debugDraw = new SlickDebugDraw(null,null)

  // TestBedMain API
  val g = new DebugDraw(debugDraw)
  def mouseX = mousePos.x
  def mouseY = mousePos.y
  def pmouseX = mousePosOld.x
  def pmouseY = mousePosOld.y
  def shiftKey = shiftDown
  def width = gameContainer.getWidth
  def height = gameContainer.getHeight

  var shiftDown = false

  var mouseButton = 0
  var mousePressed = false
  var mousePos = Vector2f.Zero
  var mousePosOld = Vector2f.Zero
  var gameContainer: GameContainer = null
  
  val tests = new collection.mutable.ListBuffer[AbstractExample]
  var currentTestIndex = 0
  var currentTest : AbstractExample = null

  def init(container: GameContainer) {

    debugDraw.g = container.getGraphics
    debugDraw.container = container

    gameContainer = container
    container.setTargetFrameRate(60)
    container.setShowFPS(false)

    tests += new testbed.tests.Bridge(this)
    tests += new testbed.tests.CCDTest(this)
    tests += new testbed.tests.Chain(this)
    tests += new testbed.tests.Circles(this)
    tests += new testbed.tests.Domino(this)
    tests += new testbed.tests.Overhang(this)
    tests += new testbed.tests.Pyramid(this)
    tests += new testbed.tests.VaryingFriction(this)
    tests += new testbed.tests.VaryingRestitution(this)
    tests += new testbed.tests.VerticalStack(this)

    currentTest = tests(currentTestIndex)
    currentTest.initialize
    initWrapper(container, currentTest.settings)
  }

  def update(container: GameContainer, delta: Int) {

    Vector2f.creationCount = 0;

    if (settings.reset || settings.testIndex != currentTestIndex) {
      currentTestIndex = settings.testIndex
      currentTest = tests(currentTestIndex)
      currentTest.initialize()
      currentTest.settings = settings
      currentTest.nanoStart = System.nanoTime()
      currentTest.frameCount = 0
    }

  }

  def render(container: GameContainer, g: Graphics) {

    // Take our time step (drawing is done here, too)
    currentTest.step

    // If the user wants to move the canvas, do it
    handleCanvasDrag

    // Draw the GUI
    drawGUI

  }

  /**
   * Handle mouseDown events.
   * @param p The screen location that the mouse is down at.
   */
  override def mousePressed(b: Int, x: Int, y: Int) {
    mouseButton = b
    mousePressed = true
    mousePosOld = mousePos
    mousePos = (x,y)
    currentTest.mouseDown(mousePos)
    desk.fireMousePressedEvent(x, container.getHeight - y, b, 1)
  }

  /**
   * Handle mouseUp events.
   */
  override def mouseReleased(b: Int, x: Int, y: Int) {
    mousePosOld = mousePos
    mousePos = (x,y)
    mousePressed = false
    currentTest.mouseUp()
    desk.fireMouseReleasedEvent(x, container.getHeight - y, b, 1)
  }

  /**
   * Handle mouseMove events (TestbedMain also sends mouseDragged events here)
   * @param p The new mouse location (screen coordinates)
   */
  override def mouseMoved(oldX: Int, oldY: Int, x: Int, y: Int) {
    mousePosOld = mousePos
    mousePos = (x,y)
    currentTest.mouseMove(mousePos)

    if (container.getInput.isMouseButtonDown(0)) {
      desk.fireMouseDraggedEvent(x, container.getHeight - y, MouseButton.LEFT, 0)
    } else {
      desk.fireMouseMovedEvent(x, container.getHeight - y)
    }
  }

  override def mouseClicked(button: Int, x: Int, y: Int, clickCount: Int) {
    desk.fireMouseClickEvent(x, container.getHeight-y, button, clickCount)
  }

  /**
   * Apply keyboard shortcuts, do keypress handling, and then
   * send the key event to the current test if appropriate.
   */
	override def keyPressed(keyCode: Int, key: Char) {
    
    import org.newdawn.slick.Input
    if (keyCode == Input.KEY_LSHIFT) {
      shiftDown = true
    }
 
    if (key == 'r') settings.reset = true;
    if (key == ' ') currentTest.launchBomb();
    if (key == 'p') {
      currentTest.settings.pause = !currentTest.settings.pause;
    }
    if (key == '+' && currentTest.settings.pause) {
      currentTest.settings.singleStep = true;
    }
    if (key == 's') currentTest.settings.drawStats = !currentTest.settings.drawStats;
    if (key == 'c') currentTest.settings.drawContactPoints = !currentTest.settings.drawContactPoints;
    if (key == 'b') currentTest.settings.drawAABBs = !currentTest.settings.drawAABBs;

    desk.fireKeyPressedEvent(EventHelper.mapKeyChar, EventHelper.mapEventKey)
    desk.fireKeyTypedEvent(EventHelper.mapKeyChar)
  }
 
  override def keyReleased(keyCode: Int, key: Char) {
    import org.newdawn.slick.Input
    if (keyCode == Input.KEY_LSHIFT) {
      shiftDown = false
    }
    desk.fireKeyReleasedEvent(EventHelper.mapKeyChar, EventHelper.mapEventKey)
  }

  override def mouseWheelMoved(amount: Int) {

    val d = debugDraw
    val notches = amount
    val oldCenter = d.screenToWorld(width / 2.0f, height / 2.0f)
    //Change the zoom and clamp it to reasonable values
    if (notches < 0) {
      d.scaleFactor = Math.min(300f, d.scaleFactor * 1.05f);
    }
    else if (notches > 0) {
      d.scaleFactor = Math.max(.02f, d.scaleFactor / 1.05f);
    }
    val newCenter = d.screenToWorld(width / 2.0f, height / 2.0f);
    d.transX -= (oldCenter.x - newCenter.x) * d.scaleFactor;
    d.transY -= (oldCenter.y - newCenter.y) * d.scaleFactor;
    currentTest.cachedCamScale = d.scaleFactor;

    desk.fireMouseWheel(container.getInput.getMouseX,
                        container.getHeight - container.getInput.getMouseY,
                        amount > 0, amount.abs, 0)
  }
  
  /**
   * Allows the world to be dragged with a right-click.
   */
  def handleCanvasDrag() {
    //Handle mouse dragging stuff
    //Left mouse attaches mouse joint to object.
    //Right mouse drags canvas.
    val d = debugDraw
		
    if (mouseButton == 1) {
      if (mousePressed) {
        d.transX += mouseX - pmouseX;
        d.transY -= mouseY - pmouseY;
        val v = d.screenToWorld(width*.5f,height*.5f);
        currentTest.cachedCamX = v.x;
        currentTest.cachedCamY = v.y;
      }
    }
  }
}