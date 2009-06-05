package org.villane.box2d.testbed.fenggui

import org.fenggui.{FengGUI,Label,Display, Button, Container}
import org.fenggui.layout.{RowExLayout, RowExLayoutData, RowLayout, FormLayout}
import org.fenggui.util.{Alignment, Spacing}
import org.fenggui.binding.render.Binding
import org.fenggui.binding.render.lwjgl.{LWJGLBinding,EventHelper}
import org.fenggui.event.mouse.MouseButton
import org.fenggui.theme.{DefaultTheme, XMLTheme}
import org.fenggui.decorator.background.PlainBackground

import org.newdawn.slick.{GameContainer,Graphics,Input,InputListener,Color}
import org.newdawn.slick.opengl.SlickCallable

trait FengWrapper extends InputListener {
  
  var container: GameContainer = null
  var desk : Display = null
  var input: Input = null

  def initWrapper(container: GameContainer) {
    this.container = container
    container.getInput.addPrimaryListener(this)
    container.getInput.enableKeyRepeat(500, 30)
    desk = new Display(new LWJGLBinding)
    buildGUI
  }

  def buildGUI {
    
    //FengGUI.setTheme(new DefaultTheme())
    Binding.getInstance().setUseClassLoader(true)
    FengGUI.setTheme(new XMLTheme("themes/QtCurve/QtCurve.xml"))
    val w = FengGUI.createWindow(false, false)
    w.setTitle("ScalaBox2D TestBed")
    w.getContentContainer.setLayoutManager(new RowLayout(false))
    
    val c = new Container
    //c.getAppearance.add(new PlainBackground(org.fenggui.util.Color.DARK_GREEN))
    //c.setExpandable(false)
    
    val play = FengGUI.createWidget(classOf[Button])
    play.setText("Play")
    play.getAppearance.setMargin(new Spacing(5, 2))
    //play.setSizeToMinSize
    //play.setExpandable(false)

    val step = FengGUI.createWidget(classOf[Button])
    step.setText("Step")
    step.getAppearance.setMargin(new Spacing(5, 2))
    //step.setExpandable(false)

    val restart = FengGUI.createWidget(classOf[Button])
    restart.setText("Restart")
    restart.getAppearance.setMargin(new Spacing(5, 2))
    //restart.setExpandable(false)

    c.addWidget(play, step, restart)
    c.pack
    
    w.getContentContainer.addWidget(c)
    w.setWidth(200)
    //w.layout
    w.pack
    val y = w.getHeight
    val x = w.getWidth
    w.setXY(600-x, 600-y)
    desk.addWidget(w)
  }

  def draw() {
    SlickCallable.enterSafeBlock
    desk.display
    SlickCallable.leaveSafeBlock
  }

  implicit def slickButton2Feng(button: Int) = button match {
    case 1 => MouseButton.RIGHT
    case 2 => MouseButton.MIDDLE
    case _ => MouseButton.LEFT
  }

  override def isAcceptingInput = true
  override def setInput(input: Input) = this.input = input
  override def inputEnded {}

  override def mouseMoved(oldX: Int, oldY: Int, newX: Int, newY: Int) {
    if (container.getInput.isMouseButtonDown(0))
    desk.fireMouseDraggedEvent(newX, container.getHeight - newY, MouseButton.LEFT, 0)
    else
    desk.fireMouseMovedEvent(newX, container.getHeight - newY)
  }
  override def mousePressed(button: Int, x: Int, y: Int) {
    desk.fireMousePressedEvent(x, container.getHeight - y, button, 1)     
  }
  override def mouseReleased(button: Int, x: Int, y: Int) {
    desk.fireMouseReleasedEvent(x, container.getHeight - y, button, 1)
  }
  override def mouseClicked(button: Int, x: Int, y: Int, clickCount: Int) {
    //desk.fireMouseClickEvent(x, y, button, clickCount)
  }
  override def mouseWheelMoved(change: Int) {
    desk.fireMouseWheel(container.getInput.getMouseX,
                        container.getHeight - container.getInput.getMouseY,
                        change > 0, change.abs, 0) 
  }

  override def keyPressed(key: Int, c: Char) {
    desk.fireKeyPressedEvent(EventHelper.mapKeyChar, EventHelper.mapEventKey)
    desk.fireKeyTypedEvent(EventHelper.mapKeyChar)
  }
  override def keyReleased(key: Int, c: Char) {
    desk.fireKeyReleasedEvent(EventHelper.mapKeyChar, EventHelper.mapEventKey)
  }

  override def controllerLeftPressed(controller: Int) {}
  override def controllerLeftReleased(controller: Int) {}
  override def controllerRightPressed(controller: Int) {}
  override def controllerRightReleased(controller: Int) {}
  override def controllerUpPressed(controller: Int) {}
  override def controllerUpReleased(controller: Int) {}
  override def controllerDownPressed(controller: Int) {}
  override def controllerDownReleased(controller: Int) {}
  override def controllerButtonPressed(controller: Int, button: Int) {}
  override def controllerButtonReleased(controller: Int, button: Int) {}
}
