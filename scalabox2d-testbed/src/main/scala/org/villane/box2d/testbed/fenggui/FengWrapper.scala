package org.villane.box2d.testbed.fenggui

import org.fenggui.{FengGUI, Label, Display, Button}
import org.fenggui.{Container, ComboBox, TextEditor, CheckBox}
import org.fenggui.layout.{RowExLayout, RowExLayoutData, RowLayout, FormLayout}
import org.fenggui.util.{Alignment, Spacing}
import org.fenggui.binding.render.Binding
import org.fenggui.binding.render.lwjgl.{LWJGLBinding,EventHelper}
import org.fenggui.event.mouse.MouseButton
import org.fenggui.theme.{DefaultTheme, XMLTheme}
import org.fenggui.decorator.background.PlainBackground
import org.fenggui.decorator.border.TitledBorder

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
    w.setTitle("Control Panel")
    w.getContentContainer.setLayoutManager(new RowLayout(false))
    
    val buttons = FengGUI.createWidget(classOf[Container])
    //c.setExpandable(false)

    val play = FengGUI.createWidget(classOf[Button])
    play.setText("Play")
    play.getAppearance.setMargin(new Spacing(5, 2))

    val step = FengGUI.createWidget(classOf[Button])
    step.setText("Step")
    step.getAppearance.setMargin(new Spacing(5, 2))

    val restart = FengGUI.createWidget(classOf[Button])
    restart.setText("Restart")
    restart.getAppearance.setMargin(new Spacing(5, 2))

    buttons.addWidget(play, step, restart)

    val spacer2 = FengGUI.createWidget(classOf[Container])
    spacer2.getAppearance.add(new TitledBorder("Tests"))

    val testList = FengGUI.createWidget(classOf[ComboBox])
    testList.getAppearance.setMargin(new Spacing(5, 2))

    spacer2.addWidget(testList)
    testList.addItem("Bridge")
    testList.addItem("CCDTest")
    testList.addItem("Chain")
    testList.addItem("Circles")
    testList.addItem("Circular Breakout")
    testList.addItem("Domino")
    testList.addItem("Overhang")
    testList.addItem("Pyramid")
    testList.addItem("Varying Friction")
    testList.addItem("Varying Restitution")
    testList.addItem("Vertical Stack")

    val tuning = FengGUI.createWidget(classOf[Container])
    tuning.getAppearance.add(new TitledBorder("Tuning"))
    tuning.setLayoutManager(new RowLayout(false))

    val t1 = FengGUI.createWidget(classOf[Container])

    val gravity = FengGUI.createWidget(classOf[Label])
    gravity.setText("Gravity")
    val gCell = FengGUI.createWidget(classOf[TextEditor])
    gCell.setRestrict(TextEditor.RESTRICT_NUMBERSONLY)
    gCell.setMaxCharacters(5)
    gCell.updateMinSize()
    gCell.setText("9.81")
    
    val hertz = FengGUI.createWidget(classOf[Label])
    hertz.setText("Hertz")
    val hCell = FengGUI.createWidget(classOf[TextEditor])
    hCell.setRestrict(TextEditor.RESTRICT_NUMBERSONLY)
    hCell.setMaxCharacters(5)
    hCell.updateMinSize()
    hCell.setText("60.0")
    t1.addWidget(gravity, gCell, hertz, hCell)

    val t2 = FengGUI.createWidget(classOf[Container])

    val velIters = FengGUI.createWidget(classOf[Label])
    velIters.setText("Vel Iters")
    val vCell = FengGUI.createWidget(classOf[TextEditor])
    vCell.setRestrict(TextEditor.RESTRICT_NUMBERSONLY)
    vCell.setMaxCharacters(2)
    vCell.updateMinSize()
    vCell.setText("10")

    val posIters = FengGUI.createWidget(classOf[Label])
    posIters.setText("Pos Iters")
    val pCell = FengGUI.createWidget(classOf[TextEditor])
    pCell.setRestrict(TextEditor.RESTRICT_NUMBERSONLY)
    pCell.setMaxCharacters(2)
    pCell.updateMinSize()
    pCell.setText("2")
    t2.addWidget(velIters, vCell, posIters, pCell)

    val sleeping = FengGUI.createCheckBox
    sleeping.setText("Sleeping")
    sleeping.setSelected(true)

    val warmStarting = FengGUI.createCheckBox
    warmStarting.setText("Warm Starting")
    warmStarting.setSelected(true)

    val timeOfImpact = FengGUI.createCheckBox
    timeOfImpact.setText("Time of Impact")
    timeOfImpact.setSelected(true)

    tuning.addWidget(t1, t2, sleeping, warmStarting, timeOfImpact)

    val draw = FengGUI.createWidget(classOf[Container])
    draw.getAppearance.add(new TitledBorder("Draw"))
    draw.setLayoutManager(new RowLayout(false))

    val shapes = FengGUI.createCheckBox
    shapes.setText("Shapes")
    shapes.setSelected(true)

    val joints = FengGUI.createCheckBox
    joints.setText("Joints")
    joints.setSelected(true)

    val controllers = FengGUI.createCheckBox
    controllers.setText("Controllers")
    controllers.setSelected(false)

    val coreShapes = FengGUI.createCheckBox
    coreShapes.setText("Core Shapes")
    coreShapes.setSelected(false)

    val aabb = FengGUI.createCheckBox
    aabb.setText("AABBs")
    aabb.setSelected(false)

    val obb = FengGUI.createCheckBox
    obb.setText("OBBs")
    obb.setSelected(false)

    val pairs = FengGUI.createCheckBox
    pairs.setText("Pairs")
    pairs.setSelected(false)

    val cPoints = FengGUI.createCheckBox
    cPoints.setText("Contact Points")
    cPoints.setSelected(false)

    val cNormals = FengGUI.createCheckBox
    cNormals.setText("Contact Normals")
    cNormals.setSelected(false)

    val cForces = FengGUI.createCheckBox
    cForces.setText("Contact Forces")
    cForces.setSelected(false)

    val fForces = FengGUI.createCheckBox
    fForces.setText("Friction Forces")
    fForces.setSelected(false)

    val com = FengGUI.createCheckBox
    com.setText("Center of Masses")
    com.setSelected(false)

    val stastics = FengGUI.createCheckBox
    stastics.setText("Stastics")
    stastics.setSelected(false)

    draw.addWidget(shapes, joints, controllers, coreShapes, aabb, obb, pairs)
    draw.addWidget(cPoints,cNormals,cForces,fForces,com,stastics)

    w.getContentContainer.addWidget(buttons,spacer2,tuning, draw)
    w.setWidth(200)
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
