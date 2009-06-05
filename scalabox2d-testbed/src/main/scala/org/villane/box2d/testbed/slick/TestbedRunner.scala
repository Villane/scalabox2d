package org.villane.box2d.testbed.slick

import vecmath._
import vecmath.Preamble._
import box2d.settings.Settings
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

object TestbedRunner {
  def main(args: Array[String]) {
    val container = new AppGameContainer(new SlickTestGame())
    container.setDisplayMode(600,600,false)
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

  val m_debugDraw = new SlickDebugDraw(null,null)

  // TestBedMain API
  val g = new DebugDraw(m_debugDraw)
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

  /** FPS that we want to achieve */
  val targetFPS = 60.0f;
  /** Number of frames to average over when computing real FPS */
  val fpsAverageCount = 100;
  /** Array of timings */
  var nanos: Array[Long]= null;
  /** When we started the nanotimer */
  var nanoStart = 0L; //
    
  /** Number of frames since we started this example. */
	var frameCount = 0L;

  val tests = new collection.mutable.ListBuffer[AbstractExample]
  var currentTestIndex = -1
  def getCurrentTest = if (tests.isDefinedAt(currentTestIndex))
  Some(tests(currentTestIndex))
  else
  None

  def init(container: GameContainer) {

    m_debugDraw.g = container.getGraphics
    m_debugDraw.container = container

    gameContainer = container
    container.setTargetFrameRate(60)

    tests += new testbed.tests.CircularBreakout(this)
    tests += new testbed.tests.Domino(this)
    tests += new testbed.tests.Pyramid(this)
    tests += new testbed.tests.Bridge(this)
    tests += new testbed.tests.Chain(this)
    tests += new testbed.tests.CCDTest(this)
    tests += new testbed.tests.Circles(this)
    tests += new testbed.tests.Overhang(this)
    tests += new testbed.tests.VaryingFriction(this)
    tests += new testbed.tests.VaryingRestitution(this)
    tests += new testbed.tests.VerticalStack(this)

    /* Set up the timers for FPS reporting */
    nanos = new Array[Long](fpsAverageCount)
    val nanosPerFrameGuess = (1000000000.0 / targetFPS).toLong
    nanos(fpsAverageCount-1) = System.nanoTime();
    for (i <- new Range.Inclusive(fpsAverageCount-2,0,-1)) {
      nanos(i) = nanos(i+1) - nanosPerFrameGuess;
    }
    nanoStart = System.nanoTime();

    initWrapper(container)
  }

  def update(container: GameContainer, delta: Int) {}

  def render(container: GameContainer, g: Graphics) {

    m_debugDraw.g = g;
    m_debugDraw.container = container;
    //background(0);
    Vector2f.creationCount = 0;

    /* Make sure we've got a valid test to run and reset it if needed */
    getCurrentTest match {
      case None =>  
        currentTestIndex = 0
        nanoStart = System.nanoTime();
        frameCount = 0;
      case _ =>
    }
    val currentTest = getCurrentTest.get

    if (currentTest.needsReset) {
      //System.out.println("Resetting "+currentTest.name);
      val s = currentTest.settings; //copy settings
      currentTest.initialize();
      if (s != null) currentTest.settings = s;
      nanoStart = System.nanoTime();
      frameCount = 0;
    }

    currentTest.m_textLine = AbstractExample.textLineHeight;
    //g.setColor(AbstractExample.white)
    g.drawString(currentTest.name, 5, currentTest.m_textLine);
    currentTest.m_textLine += 2*AbstractExample.textLineHeight;

    /* Take our time step (drawing is done here, too) */
    currentTest.step();

    /* If the user wants to move the canvas, do it */
    handleCanvasDrag();


    /* ==== Vec2 creation and FPS reporting ==== */
    if (currentTest.settings.drawStats) {
      //g.setColor(AbstractExample.white)
      g.drawString("Vec2 creations/frame: "+Vector2f.creationCount, 5, currentTest.m_textLine);
      currentTest.m_textLine += AbstractExample.textLineHeight;
    }

    for (i <- 0 until fpsAverageCount-1) {
      nanos(i) = nanos(i+1)
    }
    nanos(fpsAverageCount-1) = System.nanoTime();
    val averagedFPS = ( (fpsAverageCount-1) * 1000000000.0 / (nanos(fpsAverageCount-1)-nanos(0)));
    frameCount += 1
    val totalFPS = (frameCount * 1000000000 / (1.0*(System.nanoTime()-nanoStart)));
    if (currentTest.settings.drawStats) {
      //g.setColor(AbstractExample.white)
      g.drawString("Average FPS ("+fpsAverageCount+" frames): "+averagedFPS, 5, currentTest.m_textLine);
      currentTest.m_textLine += AbstractExample.textLineHeight;
      //g.setColor(AbstractExample.white)
      g.drawString("Average FPS (entire test): "+totalFPS, 5, currentTest.m_textLine);
      currentTest.m_textLine += AbstractExample.textLineHeight;
    }
    
    draw
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
    getCurrentTest match {
      case Some(test) =>
        test.mouseDown(mousePos)
      case None =>
    }
  }

  /**
   * Handle mouseUp events.
   */
  override def mouseReleased(b: Int, x: Int, y: Int) {
    mousePosOld = mousePos
    mousePos = (x,y)
    mousePressed = false
    getCurrentTest match {
      case Some(test) =>
        test.mouseUp()
      case None =>
    }
  }

  /**
   * Handle mouseMove events (TestbedMain also sends mouseDragged events here)
   * @param p The new mouse location (screen coordinates)
   */
  override def mouseMoved(oldX: Int, oldY: Int, x: Int, y: Int) {
    mousePosOld = mousePos
    mousePos = (x,y)
    getCurrentTest match {
      case Some(test) =>
        test.mouseMove(mousePos)
      case None =>
    }
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
    if (keyCode == Input.KEY_RIGHT) {
      currentTestIndex += 1
      if (currentTestIndex >= tests.size) currentTestIndex = 0;
      getCurrentTest.get.needsReset = true;
      return;
    } else if (keyCode == Input.KEY_LEFT) {
      currentTestIndex -= 1
      if (currentTestIndex < 0) currentTestIndex = tests.size-1;
      getCurrentTest.get.needsReset = true;
      return;
    }
    getCurrentTest match {
      case None =>
      case Some(currentTest) =>
        if (key == 'r') currentTest.needsReset = true;
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
    }
  }
 
  override def keyReleased(keyCode: Int, key: Char) {
	  import org.newdawn.slick.Input
    if (keyCode == Input.KEY_LSHIFT) {
      shiftDown = false
    }
  }

  override def mouseWheelMoved(amount: Int) {
    getCurrentTest match {
      case Some(test) =>
        val d = m_debugDraw
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
        test.cachedCamScale = d.scaleFactor;
      case None =>
    }
  }
  /**
   * Allows the world to be dragged with a right-click.
   */
  def handleCanvasDrag() {
    //Handle mouse dragging stuff
    //Left mouse attaches mouse joint to object.
    //Right mouse drags canvas.
    val d = m_debugDraw
		
    //Vec2 mouseWorld = d.screenToWorld(mouseX, mouseY);
    if (mouseButton == 1) {
      if (mousePressed) {
        d.transX += mouseX - pmouseX;
        d.transY -= mouseY - pmouseY;
        val v = d.screenToWorld(width*.5f,height*.5f);
        getCurrentTest.get.cachedCamX = v.x;
        getCurrentTest.get.cachedCamY = v.y;
      }
    }
    
  }
    
    
}
