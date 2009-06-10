package org.villane.box2d.testbed.fenggui

import org.villane.vecmath.Vector2f

import org.fenggui.{FengGUI, Label, Display, Button, List}
import org.fenggui.{Container, ComboBox, TextEditor, CheckBox}
import org.fenggui.layout.{RowExLayout, RowExLayoutData, RowLayout, FormLayout}
import org.fenggui.util.{Alignment, Spacing}
import org.fenggui.binding.render.Binding
import org.fenggui.binding.render.lwjgl.{LWJGLBinding,EventHelper}
import org.fenggui.theme.{DefaultTheme, XMLTheme}
import org.fenggui.decorator.background.PlainBackground
import org.fenggui.decorator.border.TitledBorder
import org.fenggui.event.mouse.MouseButton
import org.fenggui.event.{ISelectionChangedListener, SelectionChangedEvent}
import org.fenggui.event.{IButtonPressedListener, ButtonPressedEvent}
import org.fenggui.event.{ITextChangedListener, TextChangedEvent}

import org.newdawn.slick.{GameContainer,Graphics,Input,InputListener,Color}
import org.newdawn.slick.opengl.SlickCallable

import testbed.TestSettings
import slick.SlickTestGame

trait FengWrapper extends InputListener {
  
  var container: GameContainer = null
  var desk : Display = null
  var input: Input = null
  var settings : TestSettings = null

  def initWrapper(container: GameContainer, settings: TestSettings) {
    this.container = container
    this.settings = settings
    container.getInput.addPrimaryListener(this)
    container.getInput.enableKeyRepeat(500, 30)
    desk = new Display(new LWJGLBinding)
    buildGUI
  }

  def buildGUI {
    
    Binding.getInstance().setUseClassLoader(true)
    FengGUI.setTheme(new XMLTheme("themes/QtCurve/QtCurve.xml"))
    val w = FengGUI.createWindow(false, false)
    w.setTitle("Control Panel")
    w.getContentContainer.setLayoutManager(new RowLayout(false))
    
    val buttons = FengGUI.createWidget(classOf[Container])

    val pause = FengGUI.createWidget(classOf[Button])
    pause.setText("Pause")
    pause.getAppearance.setMargin(new Spacing(5, 2))

    pause.addButtonPressedListener(new IButtonPressedListener() {
      def buttonPressed(e: ButtonPressedEvent) {
        settings.pause = !settings.pause
      }
    })

    val step = FengGUI.createWidget(classOf[Button])
    step.setText("Step")
    step.getAppearance.setMargin(new Spacing(5, 2))

    step.addButtonPressedListener(new IButtonPressedListener() {
      def buttonPressed(e: ButtonPressedEvent) {
        settings.singleStep = true
        settings.pause = true
      }
    })

    val reset = FengGUI.createWidget(classOf[Button])
    reset.setText("Reset")
    reset.getAppearance.setMargin(new Spacing(5, 2))

    reset.addButtonPressedListener(new IButtonPressedListener() {
      def buttonPressed(e: ButtonPressedEvent) {
        settings.reset = !settings.reset
      }
    })

    buttons.addWidget(pause, step, reset)

    val spacer2 = FengGUI.createWidget(classOf[Container])
    spacer2.getAppearance.add(new TitledBorder("Tests"))

    val testList = FengGUI.createWidget(classOf[ComboBox])
    spacer2.addWidget(testList)
    
    testList.getAppearance.setMargin(new Spacing(5, 2))
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

    testList.addSelectionChangedListener(new ISelectionChangedListener() {
        def selectionChanged(e:SelectionChangedEvent) {
          // dont listen to de-select events!
          if (!e.isSelected) return
          // TODO: bug the FengGUI guys about this nasty hack....
          val test = e.getSource.asInstanceOf[List].getSelectedItem.getText
          settings.testIndex = test match {
            case "Bridge" => 3
            case "CCDTest" => 5
            case "Chain" => 4
            case "Circles" => 6
            case "Circular Breakout" => 0
            case "Domino" => 1
            case "Overhang" => 7
            case "Pyramid" => 2
            case "Varying Friction" => 8
            case "Varying Restitution" => 9
            case "Vertical Stack" => 10
            case _ => 0
          }
        
        }
      })

    val tuning = FengGUI.createWidget(classOf[Container])
    tuning.getAppearance.add(new TitledBorder("Tuning"))
    tuning.setLayoutManager(new RowLayout(false))

    val t1 = FengGUI.createWidget(classOf[Container])

    val gravity = FengGUI.createWidget(classOf[Label])
    gravity.setText("Gravity    ")
    val gCell = FengGUI.createWidget(classOf[TextEditor])
    gCell.setRestrict(TextEditor.RESTRICT_NUMBERSONLYDECIMAL)
    gCell.setMaxCharacters(6)
    gCell.updateMinSize()
    gCell.addTextChangedListener(new ITextChangedListener() {
      def textChanged(e:TextChangedEvent) {
        val s = e.getTrigger.getText.replaceAll("\n","")
        settings.gravity = Vector2f(0f, s.toFloat)
      }
    })
    
    val hertz = FengGUI.createWidget(classOf[Label])
    hertz.setText("Hertz      ")
    val hCell = FengGUI.createWidget(classOf[TextEditor])
    hCell.setRestrict(TextEditor.RESTRICT_NUMBERSONLYDECIMAL)
    hCell.setMaxCharacters(3)
    hCell.updateMinSize()
    hCell.addTextChangedListener(new ITextChangedListener() {
      def textChanged(e:TextChangedEvent) {
        val s = e.getTrigger.getText.replaceAll("\n","")
        settings.hz = s.toInt
      }
    })

    t1.addWidget(gravity, gCell)
    t1.setExpandable(false)

    val t2 = FengGUI.createWidget(classOf[Container])
    t2.addWidget(hertz, hCell)
    t2.setExpandable(false)

    val t3 = FengGUI.createWidget(classOf[Container])

    val velIters = FengGUI.createWidget(classOf[Label])
    velIters.setText("Iterations")
    val vCell = FengGUI.createWidget(classOf[TextEditor])
    vCell.setRestrict(TextEditor.RESTRICT_NUMBERSONLY)
    vCell.setMaxCharacters(3)
    vCell.updateMinSize()
    vCell.addTextChangedListener(new ITextChangedListener() {
      def textChanged(e:TextChangedEvent) {
        val s = e.getTrigger.getText.replaceAll("\n","")
        settings.iterationCount = s.toInt
      }
    })
    //vCell.setExpandable(false)

    // TODO: implement position iterations 
    val posIters = FengGUI.createWidget(classOf[Label])
    posIters.setText("Pos Iters")
    val pCell = FengGUI.createWidget(classOf[TextEditor])
    pCell.setRestrict(TextEditor.RESTRICT_NUMBERSONLYDECIMAL)
    pCell.setMaxCharacters(3)
    pCell.updateMinSize()
    // TODO - Add positionIteration listener
    
    t3.addWidget(velIters, vCell)
    t3.setExpandable(false)
    
    gCell.setText("-10")
    hCell.setText("60")
    vCell.setText("10")
    pCell.setText("10")

    val sleeping = FengGUI.createCheckBox
    sleeping.setText("Sleeping")
    sleeping.setSelected(true)
    sleeping.addSelectionChangedListener(new ISelectionChangedListener() {
        def selectionChanged(selectionChangedEvent:SelectionChangedEvent) {
          settings.enableSleeping = !settings.enableSleeping
        }
      })

    val warmStarting = FengGUI.createCheckBox
    warmStarting.setText("Warm Starting")
    warmStarting.setSelected(true)
    warmStarting.addSelectionChangedListener(new ISelectionChangedListener() {
        def selectionChanged(e:SelectionChangedEvent) {
          settings.enableWarmStarting = !settings.enableWarmStarting
        }
    })

    val timeOfImpact = FengGUI.createCheckBox
    timeOfImpact.setText("Time of Impact")
    timeOfImpact.setSelected(true)
    timeOfImpact.addSelectionChangedListener(new ISelectionChangedListener() {
        def selectionChanged(e:SelectionChangedEvent) {
          settings.enableTOI = !settings.enableTOI
        }
    })

    tuning.addWidget(t1, t2, t3, sleeping, warmStarting, timeOfImpact)

    val draw = FengGUI.createWidget(classOf[Container])
    draw.getAppearance.add(new TitledBorder("Draw"))
    draw.setLayoutManager(new RowLayout(false))

    val shapes = FengGUI.createCheckBox
    shapes.setText("Shapes")
    shapes.setSelected(true)
    shapes.addSelectionChangedListener(new ISelectionChangedListener() {
        def selectionChanged(e:SelectionChangedEvent) {
          settings.drawShapes = !settings.drawShapes
        }
    })

    val joints = FengGUI.createCheckBox
    joints.setText("Joints")
    joints.setSelected(true)
    joints.addSelectionChangedListener(new ISelectionChangedListener() {
        def selectionChanged(e:SelectionChangedEvent) {
          settings.drawJoints = !settings.drawJoints
        }
    })

    val coreShapes = FengGUI.createCheckBox
    coreShapes.setText("Core Shapes")
    coreShapes.setSelected(false)
    coreShapes.addSelectionChangedListener(new ISelectionChangedListener() {
        def selectionChanged(e:SelectionChangedEvent) {
          settings.drawCoreShapes = !settings.drawCoreShapes
        }
    })

    val aabb = FengGUI.createCheckBox
    aabb.setText("AABBs")
    aabb.setSelected(false)
    aabb.addSelectionChangedListener(new ISelectionChangedListener() {
        def selectionChanged(e:SelectionChangedEvent) {
          settings.drawAABBs = !settings.drawAABBs
        }
    })

    val obb = FengGUI.createCheckBox
    obb.setText("OBBs")
    obb.setSelected(false)
    obb.addSelectionChangedListener(new ISelectionChangedListener() {
        def selectionChanged(e:SelectionChangedEvent) {
          settings.drawOBBs = !settings.drawOBBs
        }
    })

    val pairs = FengGUI.createCheckBox
    pairs.setText("Pairs")
    pairs.setSelected(false)
    pairs.addSelectionChangedListener(new ISelectionChangedListener() {
        def selectionChanged(e:SelectionChangedEvent) {
          settings.drawPairs = !settings.drawPairs
        }
    })

    val cPoints = FengGUI.createCheckBox
    cPoints.setText("Contact Points")
    cPoints.setSelected(false)
    cPoints.addSelectionChangedListener(new ISelectionChangedListener() {
        def selectionChanged(e:SelectionChangedEvent) {
          settings.drawContactPoints = !settings.drawContactPoints
        }
    })

    val cNormals = FengGUI.createCheckBox
    cNormals.setText("Contact Normals")
    cNormals.setSelected(false)
    cNormals.addSelectionChangedListener(new ISelectionChangedListener() {
        def selectionChanged(e:SelectionChangedEvent) {
          settings.drawContactNormals = !settings.drawContactNormals
        }
    })

    val com = FengGUI.createCheckBox
    com.setText("Center of Masses")
    com.setSelected(false)
    com.addSelectionChangedListener(new ISelectionChangedListener() {
        def selectionChanged(e:SelectionChangedEvent) {
          settings.drawCOMs = !settings.drawCOMs
        }
    })

    val stastics = FengGUI.createCheckBox
    stastics.setText("Stastics")
    stastics.setSelected(false)
    stastics.addSelectionChangedListener(new ISelectionChangedListener() {
        def selectionChanged(selectionChangedEvent:SelectionChangedEvent) {
          settings.drawStats = !settings.drawStats
        }
    })

    draw.addWidget(shapes, joints, coreShapes, aabb, obb, pairs)
    draw.addWidget(cPoints, cNormals, com, stastics)
    
    w.getContentContainer.addWidget(buttons, spacer2, tuning, draw)
    w.setWidth(200)
    w.pack
    val y = w.getHeight
    val x = w.getWidth
    w.setXY(800-x, 600-y)
    desk.addWidget(w)
  }

  def drawGUI() {
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
