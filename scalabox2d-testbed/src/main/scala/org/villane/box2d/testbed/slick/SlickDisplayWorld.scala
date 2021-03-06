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

object SlickDisplayWorld {
  import DrawFlags._
  def runWithSimulation(world: World, flipY: Boolean, flags: Int): Unit =
    run(world, flipY, true, flags)

  def run(world: World): Unit = run(world, true, false, Shapes)

  def run(world: World, flipY: Boolean, runSimulation: Boolean, flags: Int) {
    val app = new SlickDisplayWorld(world)
    app.simulate = runSimulation
    app.dd.flags = flags
    val container = new AppGameContainer(app)
    app.m_debugDraw.yFlip = if (flipY) -1 else 1
    container.setTargetFrameRate(60)
    container.setDisplayMode(800,800,false)
    container.start()
  }
}

class SlickDisplayWorld(world: World) extends BasicGame("Slick ScalaBox2D: World") {
  var simulate = false
  val m_debugDraw = new SlickDebugDraw(null,null)
  val dd = new DebugDraw(m_debugDraw)
  def width = gameContainer.getWidth
  def height = gameContainer.getHeight
  def mouseX = mousePos.x
  def mouseY = mousePos.y
  def pmouseX = mousePosOld.x
  def pmouseY = mousePosOld.y
  var gameContainer: GameContainer = null

  def init(container: GameContainer) {
    m_debugDraw.container = container;
    gameContainer = container
    m_debugDraw.transX = world.aabb.center.x
    m_debugDraw.transY = world.aabb.center.y
  }

  def update(container: GameContainer, delta: Int) {
    if (simulate) {
      var timeStep = 1.0f / 60
      world.step(timeStep, 10)
    }
  }

  def render(container: GameContainer, g: Graphics) {
    m_debugDraw.g = g
    handleCanvasDrag
    dd.drawDebugData(world)
  }

  var mouseButton = 0
  var mousePressed = false
  var mousePos = Vector2.Zero
  var mousePosOld = Vector2.Zero
    /**
     * Handle mouseDown events.
     * @param p The screen location that the mouse is down at.
     */
    override def mousePressed(b: Int, x: Int, y: Int) {
      mouseButton = b
      mousePressed = true
      mousePosOld = mousePos
      mousePos = (x,y)
    }

    /**
     * Handle mouseUp events.
     */
    override def mouseReleased(b: Int, x: Int, y: Int) {
      mousePosOld = mousePos
      mousePos = (x,y)
      mousePressed = false
    }

    /**
     * Handle mouseMove events (TestbedMain also sends mouseDragged events here)
     * @param p The new mouse location (screen coordinates)
     */
    override def mouseMoved(oldX: Int, oldY: Int, x: Int, y: Int) {
      mousePosOld = mousePos
      mousePos = (x,y)
    }

    def handleCanvasDrag() {
      val d = m_debugDraw
        //Vec2 mouseWorld = d.screenToWorld(mouseX, mouseY);
        if (mouseButton == 1) {
            if (mousePressed) {
                d.transX += mouseX - pmouseX;
                d.transY -= mouseY - pmouseY;
                val v = d.screenToWorld(width*.5f,height*.5f);
            }
        }
    
    }
    override def mouseWheelMoved(amount: Int) {
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
    }

}
