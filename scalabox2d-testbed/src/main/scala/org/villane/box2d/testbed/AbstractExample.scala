package org.villane.box2d.testbed

import box2d.draw._
import vecmath._
import vecmath.Preamble._
import box2d.shapes._
import box2d.dynamics._
import box2d.dynamics.joints._
import box2d.dynamics.contacts._
import box2d.settings.Settings

import scala.collection.jcl.ArrayList

// TODO import //org.newdawn.slick.Image
class Image {
  def width = 10
  def height = 10
}

object AbstractExample {
  /** General instructions that apply to all tests. */
  val instructionString = "Shift+drag to slingshot a bomb\n"
  val white = new Color3f(255.0f,255.0f,255.0f);
  val black = new Color3f(0.0f*255.0f,0.0f*255.0f,0.0f*255.0f);
  val gray = new Color3f(0.5f*255.0f,0.5f*255.0f,0.5f*255.0f);
  val red = new Color3f(255.0f,0.0f,0.0f);
  val green = new Color3f(0.0f,255.0f,0.0f);
  val blue= new Color3f(0.0f,0.0f,255.0f);
  /** Height of font used to draw text. */
  val textLineHeight = 15;
  /** Max number of contact points to store */
  val k_maxContactPoints = 2048; 
}

abstract class AbstractExample(parent: TestbedMain) {
  import AbstractExample._
  /** Used for drawing */
  var debugDraw = parent.g
  //public Body followedBody = null; //camera follows motion of this body
  /** Array of key states, by char value.  Does not include arrows or modifier keys. */
  var keyDown = new Array[Boolean](255)
  /** Same as keyDown, but true only if the key was newly pressed this frame. */
  var newKeyDown = new Array[Boolean](255)

  /** Screen coordinates of mouse */
  var mouseScreen = Vector2f.Zero
  /** World coordinates of mouse */
  var mouseWorld = Vector2f.Zero
  /** Screen coordinates of mouse on last frame */
  var pmouseScreen = Vector2f.Zero
  /** Was the mouse pressed last frame?  True if either right or left button was down. */
  var pmousePressed = false
  /** The point at which we will place a bomb when completeBombSpawn() is called. */
  var bombSpawnPoint = Vector2f.Zero
  /** True if a bomb has started spawning but has not been created yet. */
  var bombSpawning = false
  /** Number of active points in m_points array. */
  var m_pointCount = 0
  /** Array of contact points - use m_pointCount to get number of active elements.  */
  var m_points: Array[ExampleContactPoint] = null
  /** The world object this example uses. */
  var m_world: World = null
  /** The bomb body.  May be null if no bomb is active. */
  var m_bomb: Body = null
  /** Mouse joint.  May be null if mouse is not attached to anything. */
  var m_mouseJoint: PointingDeviceJoint = null
  /** Settings for this example.  This is stored and reloaded when the example restarts or we come back from another example. */ 
  var settings = new TestSettings 
  /** The bounding box for the world.  If the defaults do not work for you, overload createWorld() and set the AABB appropriately there. */
  var m_worldAABB: AABB = null
  /** The exponentially smoothed amount of free memory available to the JVM. */ 
  var memFree = 0f
   /** FPS that we want to achieve */
  val targetFPS = 60.0f;
  /** Number of frames to average over when computing real FPS */
  val fpsAverageCount = 100;
  /** Array of timings */
  var nanos: Array[Long]= null;
  /** When we started the nanotimer */
  var nanoStart = 0L
  /** Number of frames since we started this example. */
  var frameCount = 0L;

  /** Listener for body and joint destructions. */
  protected var m_destructionListener: DestructionListener = null
  /** Listener for world AABB violations. */
  protected var m_boundaryListener: BoundaryListener = null
  /** Listener for contact events. */
  protected var m_contactListener: ContactListener = null

  /** Saved camera variable so that camera stays put between reloads of example. */
  var cachedCamX, cachedCamY, cachedCamScale = 0f
  /** Have the cachedCam* variables been set for this example? */
  var hasCachedCamera = false

  /** List of images bound to bodies. */
  protected val boundImages = new ArrayList[BoundImage]()

  /**
   * Prints default instructions + specific example instructions.
   * To add instructions to your example, override getExampleInstructions()
   */
  def printInstructions() {
    val fullString = instructionString + getExampleInstructions();
    val instructionLines = fullString.split("\n")
    var currentLine = parent.height - instructionLines.length*textLineHeight*2;
    for (i <- 0 until instructionLines.length) {
      debugDraw.draw.drawString(5, currentLine, instructionLines(i), white);
      currentLine += textLineHeight;
    }
  }

  /** 
   * Returns a string containing example instructions.
   * Overload within an example to provide special instructions
   * or information. 
   * @return A string containing example instructions
   */
  def getExampleInstructions() = ""

  /** Title of example. */
  def name: String

  /**
   * Create the world geometry for each test.
   * Any custom initialization for a test should go here.
   * 
   * Called immediately after initialize(), which handles
   * generic test initialization and should usually not be overloaded.
   */
  def create()

  /** Override this if you need to create a different world AABB or gravity vector */
  def createWorld() {
    m_worldAABB = AABB((-200.0f, -100.0f),(200.0f, 200.0f))
    val gravity = (0.0f, -10.0f)
    m_world = new World(m_worldAABB, gravity, true);
  }

  /**
   * Should not usually be overloaded.
   * Performs initialization tasks common to most examples:
   * <BR>
   * <UL>
   * <LI>Resets input state
   * <LI>Initializes member variables
   * <LI>Sets test settings
   * <LI>Creates world, gravity and AABB by calling createWorld() - overload that if you are unhappy with defaults
   * <LI>Sets up and attaches listeners
   * <LI>Sets up drawing
   * <LI>Sets camera to default or saved state
   * <LI>Calls create() to set up test
   * </UL>
   */
  def initialize() {

    /* Set up the timers for FPS reporting */
    nanos = new Array[Long](fpsAverageCount)
    val nanosPerFrameGuess = (1000000000.0 / targetFPS).toLong
    nanos(fpsAverageCount-1) = System.nanoTime();
    for (i <- new Range.Inclusive(fpsAverageCount-2,0,-1)) {
      nanos(i) = nanos(i+1) - nanosPerFrameGuess;
    }
    nanoStart = System.nanoTime();
    
    settings.reset = false;
    for (i <- 0 until 255) {
      keyDown(i) = false;
      newKeyDown(i) = false;
    }
    
    settings = new TestSettings
    mouseScreen = Vector2f(parent.mouseX, parent.mouseY)
    mouseWorld = Vector2f.Zero
    pmouseScreen = Vector2f(mouseScreen.x,mouseScreen.y)
    pmousePressed = false

    createWorld

    m_bomb = null;
    m_mouseJoint = null;
    bombSpawnPoint = null;
    bombSpawning = false;

    m_points = new Array[ExampleContactPoint](k_maxContactPoints);
    for (i <- 0 until m_points.length) {
      m_points(i) = new ExampleContactPoint();
    }

    m_destructionListener = new ConcreteDestructionListener();
    m_boundaryListener = new ConcreteBoundaryListener();
    m_contactListener = new ConcreteContactListener();
    m_destructionListener.asInstanceOf[ConcreteDestructionListener].test = this;
    m_boundaryListener.asInstanceOf[ConcreteBoundaryListener].test = this;
    m_contactListener.asInstanceOf[ConcreteContactListener].test = this;
    m_world.destructionListener = m_destructionListener
    m_world.boundaryListener = m_boundaryListener
    m_world.contactListener = m_contactListener
    m_world.debugDraw = parent.g

    if (hasCachedCamera) {
      debugDraw.draw.setCamera(cachedCamX,cachedCamY,cachedCamScale);
    } else {
      debugDraw.draw.setCamera(0.0f, 10.0f, 10.0f);
      hasCachedCamera = true;
      cachedCamX = 0.0f;
      cachedCamY = 10.0f;
      cachedCamScale = 10.0f;
    }

    boundImages.clear
    create
  }

  /**
   * Take a physics step.  This is the guts of the simulation loop.
   * When creating your own game, the most important thing to have
   * in your step method is the m_world.step() call.
   */
  def step() {

    preStep();
    mouseWorld = debugDraw.draw.screenToWorld(mouseScreen)

    var timeStep = if (settings.hz > 0.0f) 1.0f / settings.hz else 0.0f

    if (settings.pause) {
      if (settings.singleStep) {
        settings.singleStep = false;
      } else {
        timeStep = 0.0f;
      }

      debugDraw.draw.drawString(2, 1, "**** PAUSED ****", white);
    }

    debugDraw.draw.drawFlags = 0
    if (settings.drawShapes) debugDraw.draw.appendFlags(DrawFlags.shape);
    if (settings.drawJoints) debugDraw.draw.appendFlags(DrawFlags.joint);
    if (settings.drawCoreShapes) debugDraw.draw.appendFlags(DrawFlags.coreShape);
    if (settings.drawAABBs) debugDraw.draw.appendFlags(DrawFlags.aabb);
    if (settings.drawOBBs) debugDraw.draw.appendFlags(DrawFlags.obb);
    if (settings.drawPairs) debugDraw.draw.appendFlags(DrawFlags.pair);
    if (settings.drawCOMs) debugDraw.draw.appendFlags(DrawFlags.centerOfMass);

    m_world.warmStarting = settings.enableWarmStarting
    m_world.positionCorrection = settings.enablePositionCorrection
    m_world.continuousPhysics = settings.enableTOI

    if(m_world.allowSleep != settings.enableSleeping && settings.enableSleeping == false) {
      for(b <- m_world.bodyList) b.wakeUp
    }
    m_world.allowSleep = settings.enableSleeping

    m_pointCount = 0;
    m_world.step(timeStep, settings.iterationCount);

    //Optional validation of broadphase - asserts if there is an error
    //m_world.m_broadPhase.validate;

    if (m_bomb != null && m_bomb.isFrozen) {
      m_world.destroyBody(m_bomb)
      m_bomb = null
    }

    for (i <- 0 until fpsAverageCount-1) {
      nanos(i) = nanos(i+1)
    }

    nanos(fpsAverageCount-1) = System.nanoTime();
    val averagedFPS = ( (fpsAverageCount-1) * 1000000000.0 / (nanos(fpsAverageCount-1)-nanos(0)));
    frameCount += 1
    val totalFPS = (frameCount * 1000000000 / (1.0*(System.nanoTime()-nanoStart)));
    
    if (settings.drawStats) {

      var textLine = 10
      debugDraw.draw.drawString(2, textLine, "proxies(max) = "+ m_world.proxyCount +
                               "("+ Settings.maxProxies+"), pairs(max) = "+ m_world.pairCount +
                               "("+ Settings.maxPairs+")", white)
      textLine += textLineHeight

      debugDraw.draw.drawString(2, textLine, "bodies/contacts/joints = "+
                               m_world.bodyList.size+"/"+m_world.contactList.size+"/"+m_world.jointList.size, white)
      textLine += textLineHeight

      val memTot = Runtime.getRuntime().totalMemory()
      memFree = (memFree * .9f + .1f * Runtime.getRuntime().freeMemory())
      debugDraw.draw.drawString(2, textLine, "total memory: "+memTot, white)
      textLine += textLineHeight
      debugDraw.draw.drawString(2, textLine, "average free memory: "+memFree, white)
      textLine += textLineHeight + 5
      debugDraw.draw.drawString(2, textLine, "Vec2 creations/frame: " + Vector2f.creationCount, white)
      textLine += textLineHeight
      debugDraw.draw.drawString(2, textLine, "Average FPS (" + fpsAverageCount + " frames): " + averagedFPS, white)
      textLine += textLineHeight
      debugDraw.draw.drawString(2, textLine, "Average FPS (entire test): "+ totalFPS, white)
    }

    if (m_mouseJoint != null) {
      val body = m_mouseJoint.body2;
      val p1 = body.toWorldPoint(m_mouseJoint.localAnchor);
      val p2 = m_mouseJoint.target;

      debugDraw.draw.drawSegment(p1, p2, new Color3f(255.0f,255.0f,255.0f));
    }

    if (bombSpawning) {
      debugDraw.draw.drawSolidCircle(bombSpawnPoint, 0.3f, Vector2f(1.0f,0.0f),Color3f(255f*0.5f,255f*0.5f,255f*0.5f));
      debugDraw.draw.drawSegment(bombSpawnPoint, mouseWorld, Color3f(55f*0.5f,55f*0.5f,255f*0.5f));
    }

    //TODO : Move this to DebugDraw
    if (settings.drawContactPoints) {
      val k_forceScale = 0.01f;
      val k_axisScale = 0.3f;

      for (i <- 0 until m_pointCount) {
        val point = m_points(i);

        if (point.state == 0) {
          // Add
          //System.out.println("Add");
          debugDraw.draw.drawPoint(point.position, 0.3f, Color3f(255.0f, 150.0f, 150.0f));
        } else if (point.state == 1) {
          // Persist
          //System.out.println("Persist");
          debugDraw.draw.drawPoint(point.position, 0.1f, Color3f(255.0f, 0.0f, 0.0f));
        } else {
          // Remove
          //System.out.println("Remove");
          debugDraw.draw.drawPoint(point.position, 0.5f, Color3f(0.0f, 155.0f, 155.0f));
        }

        if (settings.drawContactNormals) {
          val p1 = point.position;
          val p2 = Vector2f( p1.x + k_axisScale * point.normal.x,
                             p1.y + k_axisScale * point.normal.y);
          debugDraw.draw.drawSegment(p1, p2, new Color3f(0.4f*255f, 0.9f*255f, 0.4f*255f));
        } 
				//TODO
				/*else if (settings.drawContactForces) {
					Vec2 p1 = point.position;
					Vec2 p2 = new Vec2( p1.x + k_forceScale * point.normalImpulse * point.normal.x,
										p1.y + k_forceScale * point.normalImpulse * point.normal.y);
					debugDraw.drawSegment(p1, p2, new Color3f(0.9f*255f, 0.9f*255f, 0.3f*255f));
				}

				if (settings.drawFrictionForces) {
					Vec2 tangent = Vec2.cross(point.normal, 1.0f);
					Vec2 p1 = point.position;
					Vec2 p2 = new Vec2( p1.x + k_forceScale * point.tangentImpulse * tangent.x,
										p1.y + k_forceScale * point.tangentImpulse * tangent.y);
					debugDraw.drawSegment(p1, p2, new Color3f(0.9f*255f, 0.9f*255f, 0.3f*255f));
				}*/
      }
    }

    for (b <- boundImages) {
      b.draw();
    }

    printInstructions();

    pmouseScreen = (mouseScreen);
    postStep();

    //Should reset newKeyDown after postStep in case it needs to be used there
    for (i <- 0 until newKeyDown.length) {
      newKeyDown(i) = false;
    }
  }

  /** Stub for overloading in examples - called before physics step. */
  def preStep() {}

  /** Stub for overloading in examples - called after physics step. */
  def postStep() {}

  /** Space launches a bomb from a random default position. */
  def launchBomb() {
    val rnd = 0f //XXX parent.random(-15.0f, 15.0f)
    val pos = Vector2f(rnd, 30.0f);
    val vel = pos * (-5.0f)
    launchBomb(pos, vel)
  }
    	
  /** 
   * Launch bomb from a specific position with a given velocity.
   * @param position Position to launch bomb from.
   * @param velocity Velocity to launch bomb with.
   */
  def launchBomb(position: Vector2f, velocity: Vector2f) {
    if (m_bomb != null) {
      m_world.destroyBody(m_bomb);
      m_bomb = null;
    }

    val bd = new BodyDef
    bd.allowSleep = true
    bd.pos = position
    bd.isBullet = true
    m_bomb = m_world.createBody(bd)
    m_bomb.linearVelocity = velocity * 0.05f

    val sd = new CircleDef
    sd.radius = 0.3f
    sd.density = 20.0f
    sd.restitution = 1.0f //0.1f
    sd.friction = 0.0f

    val minV = position - (0.3f,0.3f)
    val maxV = position + (0.3f,0.3f)
    //AABB aabb = new AABB(minV, maxV);
    val inRange = (minV.x > m_worldAABB.lowerBound.x && minV.y > m_worldAABB.lowerBound.y &&
                       maxV.x < m_worldAABB.upperBound.x && maxV.y < m_worldAABB.upperBound.y)

    if (inRange) {
      m_bomb.createShape(sd);
      m_bomb.computeMassFromShapes
    } else {
      System.out.println("Bomb not created - out of world AABB");
    }
  }
    
  //Shift+drag "slingshots" a bomb from any point using these functions
  /**
   * Begins spawning a bomb, spawn finishes and bomb is created upon calling completeBombSpawn().
   * When a bomb is spawning, it is not an active body but its position is stored so it may be
   * drawn.
   */
  def spawnBomb(worldPt: Vector2f) {
    bombSpawnPoint = worldPt
    bombSpawning = true
  }
    
  /**
   * Creates and launches a bomb using the current bomb and mouse locations to "slingshot" it.
   */
  def completeBombSpawn() {
    if (!bombSpawning) return
    val multiplier = 30.0f;
    val mouseW = debugDraw.draw.screenToWorld(mouseScreen)
    val vel = (bombSpawnPoint - mouseW) * multiplier
    launchBomb(bombSpawnPoint,vel)
    bombSpawning = false
  }
    
    /**
     * Draws an image on a body.
     * 
     * First image is centered on body center, then
     * localScale is applied, then localOffset, and
     * lastly localRotation (all rel. to body center).
     * 
     * Thus localOffset should be specified in body
     * units to the scaled image.  For instance, if
     * you want a MxN image to have its corner
     * at body center and be scaled by S, use a localOffset
     * of (M*S/2, N*S/2) and a localScale of S.
     * 
     */
    def bindImage(p: Image, localOffset: Vector2f, localRotation:Float, localScale:Float, b:Body) {
        boundImages.add(new BoundImage(p, localOffset, localRotation, localScale, b));
    }
    
    
    /**
     * Set keyDown and newKeyDown arrays when we get a keypress.
     * @param key The key pressed.
     */
    def keyPressed(key: Int) {
        if (key >= 0 && key < 255) {
        	//System.out.println(key + " "+keyDown[key]);
            if (!keyDown(key)) newKeyDown(key) = true;
            keyDown(key) = true;
        }
    }
    
    /**
     * Set keyDown array when we get a key release.
     * @param key The key released.
     */
    def keyReleased(key: Int) {
        if (key >= 0 && key < 255) {
            keyDown(key) = false;
        }
    }
    
    /**
     * Handle mouseDown events.
     * @param p The screen location that the mouse is down at.
     */
    def mouseDown(p1: Vector2f) {
    	
    	if (parent.shiftKey) {
    		spawnBomb(debugDraw.draw.screenToWorld(p1))
    		return;
    	}
    	
    	val p = debugDraw.draw.screenToWorld(p1);
    	
    	assert (m_mouseJoint == null)

        // Make a small box.

        val d = 0.001f
        val aabb = new AABB(p - d, p + d);

        // Query the world for overlapping shapes.
        val k_maxCount = 10;
        val shapes = m_world.query(aabb, k_maxCount);
        
        var body: Body = null;
        var loop = true
        for (j <- 0 until shapes.length if loop) {
          val shapeBody = shapes(j).body
            if (shapeBody.isStatic == false) {
                val inside = shapes(j).testPoint(shapeBody.transform,p);
                if (inside) {
                    body = shapes(j).body;
                    loop = false;
                }
            }
        }

        if (body != null) {
            val md = new PointingDeviceJointDef();
            md.body1 = m_world.groundBody
            md.body2 = body;
            md.target = p
            md.maxForce = 1000.0f * body.mass;
            m_mouseJoint = m_world.createJoint(md).asInstanceOf[PointingDeviceJoint];
            body.wakeUp();
        }
    }

    /**
     * Handle mouseUp events.
     */
    def mouseUp() {
        if (m_mouseJoint != null) {
            m_world.destroyJoint(m_mouseJoint);
            m_mouseJoint = null;
        }
        if (bombSpawning) {
        	completeBombSpawn()
        }
    }

    /**
     * Handle mouseMove events (TestbedMain also sends mouseDragged events here)
     * @param p The new mouse location (screen coordinates)
     */
    def mouseMove(p: Vector2f) {
    	mouseScreen = p;
        if (m_mouseJoint != null) {
            m_mouseJoint.target = debugDraw.draw.screenToWorld(p)
        }
    }
    
    /**
     * Sets the camera target and scale.
     * 
     * @param x World x coordinate of camera focus
     * @param y World y coordinate of camera focus
     * @param scale Size in screen units (usually pixels) of one world unit (meter)
     */
    def setCamera(x: Float, y: Float, scale: Float) {
    	debugDraw.draw.setCamera(x, y, scale);
    	hasCachedCamera = true;
    	cachedCamX = x;
    	cachedCamY = y;
    	cachedCamScale = scale;
    }
	
    /** Stub method for concrete examples to override if desired.
     *  Called when a joint is implicitly destroyed due to body
     *  destruction.
     *  
     *  @param joint The implicitly destroyed joint
     */
	def jointDestroyed(joint: Joint) {
		
	}

	/** Stub method for concrete examples to override if desired.
	 *  Called when a body leaves the world boundary.
	 *  
	 * @param body The body that went out of bounds
	 */
	def boundaryViolated(body: Body) {
		
	}
    
    
	/* ==== Concrete listener classes below ====
	 * Concrete tests may override these classes, just
	 * remember to call m_world.setListener(newListener)
	 */       
	
	/**
	 * This is called when a joint in the world is implicitly destroyed
	 *	because an attached body is destroyed. This gives us a chance to
	 *	nullify the mouse joint.
	 */
    class ConcreteDestructionListener extends DestructionListener {
    	def sayGoodbye(shape: Shape) { }
    	def sayGoodbye(joint: Joint) {
    		if (test.m_mouseJoint == joint) {
    			test.m_mouseJoint = null;
    		} else {
    			test.jointDestroyed(joint);
    		}
    	}

    	var test: AbstractExample = null
    }

    /**
     * Calls boundaryViolated(Body) on violation.
     *
     */
    class ConcreteBoundaryListener extends BoundaryListener {
    	def violation(body: Body) {
    		if (test.m_bomb != body) {
    			test.boundaryViolated(body);
    		}
    	}

    	var test: AbstractExample = null
    }

    /**
     * Stores contact points away for inspection, as with CCD
     * contacts may be gone by the end of a step.
     * 
     */
    class ConcreteContactListener extends ContactListener {
    	def add(point: box2d.dynamics.contacts.ContactPoint) {
    		if (test.m_pointCount == k_maxContactPoints) {
    			return;
    		}

    		val cp = test.m_points(test.m_pointCount)
    		cp.shape1 = point.shape1;
    		cp.shape2 = point.shape2;
    		cp.position = point.pos
    		cp.normal = point.normal
    		cp.id = point.id
    		cp.velocity = point.velocity
    		cp.state = 0;

    		test.m_pointCount += 1	
    	}
    	def persist(point: box2d.dynamics.contacts.ContactPoint) {
    		if (test.m_pointCount == k_maxContactPoints) {
    			return;
    		}

    		val cp = test.m_points(test.m_pointCount)
    		cp.shape1 = point.shape1;
    		cp.shape2 = point.shape2;
    		cp.position = point.pos
    		cp.normal = point.normal
    		cp.id = point.id
    		cp.velocity = point.velocity
    		cp.state = 1;

    		test.m_pointCount += 1
    	}
    	def remove(point: box2d.dynamics.contacts.ContactPoint) {
    		if (test.m_pointCount == k_maxContactPoints) {
    			return;
    		}

    		val cp = test.m_points(test.m_pointCount);
    		cp.shape1 = point.shape1;
    		cp.shape2 = point.shape2;
    		cp.position = point.pos
    		cp.normal = point.normal
    		cp.id = point.id
    		cp.velocity = point.velocity
    		cp.state = 2;

    		test.m_pointCount += 1
    	}

    	var test: AbstractExample = null

		def result(point: ContactResult) {
			//TODO
		}
    }

    /**
     * Holder for images to be drawn on bodies.
     * You should not need to create BoundImages yourself -
     * instead, use bindImage(), which will properly
     * add the BoundImage to the ArrayList of BoundImages
     * to draw.
     * <BR><BR>
     * In a realistic application, you would also want to
     * decouple the BoundImages from bodies upon body
     * destruction; here we don't do that because this is
     * just a simple example of how to do the drawing based
     * on the body transform.  You also might want to allow
     * height/width scaling not drawn directly
     * from the image in case the image needs stretching.
     * <BR><BR>
     * Necessarily tied to ProcessingDebugDraw and
     * Processing's PImage class because of image
     * format and handling.
     * 
     */
    class BoundImage(
      val image: Image,
      val localOffset: Vector2f,
      val localRotation: Float,
      val localScale: Float,
      val body: Body
    ){
        private var halfImageWidth = image.width / 2f
        private var halfImageHeight = image.height / 2f
        private var p = debugDraw;

        def draw() {
        	//p.drawImage(image, body.pos, body.angle+localRotation, localScale, localOffset, halfImageWidth, halfImageHeight);
        }
    }
    
}
