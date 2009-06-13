package org.villane.box2d.draw

import collision._
import vecmath._
import vecmath.Preamble._
import shapes._
import dynamics._
import dynamics.joints._

object Colors {
  val Static = Color3f.ratio(0.5f, 0.9f, 0.5f)
  val Sleeping = Color3f.ratio(0.5f, 0.5f, 0.9f)
  val Dynamic = Color3f.ratio(0.9f, 0.9f, 0.9f)
  val Joint = Color3f.ratio(0.5f, 0.8f, 0.8f)
  val Core = Color3f.ratio(0.9f, 0.6f, 0.6f)
  val Pair = Color3f.ratio(0.9f, 0.9f, 0.3f)
  val AABB = Color3f(255, 255, 255)
  val WorldAABB = Color3f.ratio(0.3f, 0.9f, 0.9f)
}

object DrawFlags {
  /** draw shapes */
  val Shapes = 0x0001
  /** draw joint connections */
  val Joints = 0x0002
  /** @deprecated draw core (TOI) shapes */
  val CoreShapes = 0x0004
  /** draw axis aligned bounding boxes */
  val AABBs = 0x0008
  /** draw broad-phase pairs */
  val Pairs = 0x0020
  /** draw center of mass frame */
  val CenterOfMass = 0x0040
}

/**
 * Draws the representation of the world, given a concrete DebugDrawHandler
 */
final class DebugDraw(val handler: DebugDrawHandler) {
  import DrawFlags._
  var flags = 0

  def appendFlags(flags: Int) = this.flags |= flags
  def clearFlags(flags: Int) = this.flags &= ~flags
  def flag(flag: Int) = (this.flags & flag) == flag

  def drawDebugData(world: World) {
    drawDebugData(world.bodyList, world.jointList, world.broadPhase)
  }

  /** For internal use */
  private def drawDebugData(bodies: Seq[Body],
                            joints: Seq[Joint],
                            bp: BroadPhase) {
    if (flag(Shapes)) bodies foreach drawBody
    if (flag(Joints)) joints foreach drawJoint

    if (flag(Pairs)) {
      val invQ = 1f / bp.quantizationFactor

      // ERKKI TODO this was done differently with the custom hash table
      for (((proxyId1,proxyId2),pair) <- bp.pairManager.hashTable) {
        val p1 = bp.proxyPool(proxyId1)
        val p2 = bp.proxyPool(proxyId2)

        val b1 = AABB(
          (bp.worldAABB.lowerBound.x + invQ.x * bp.m_bounds(0)(p1.lowerBounds(0)).value,
           bp.worldAABB.lowerBound.y + invQ.y * bp.m_bounds(1)(p1.lowerBounds(1)).value),
             (bp.worldAABB.lowerBound.x + invQ.x * bp.m_bounds(0)(p1.upperBounds(0)).value,
              bp.worldAABB.lowerBound.y + invQ.y * bp.m_bounds(1)(p1.upperBounds(1)).value))
           
        val b2 = AABB(
          (bp.worldAABB.lowerBound.x + invQ.x * bp.m_bounds(0)(p2.lowerBounds(0)).value,
           bp.worldAABB.lowerBound.y + invQ.y * bp.m_bounds(1)(p2.lowerBounds(1)).value),
             (bp.worldAABB.lowerBound.x + invQ.x * bp.m_bounds(0)(p2.upperBounds(0)).value,
              bp.worldAABB.lowerBound.y + invQ.y * bp.m_bounds(1)(p2.upperBounds(1)).value))

        val x1 = 0.5f * (b1.lowerBound + b1.upperBound)
        val x2 = 0.5f * (b2.lowerBound + b2.upperBound)

        handler.drawSegment(x1, x2, Colors.Pair)
      }
    }

    val worldLower = bp.worldAABB.lowerBound
    val worldUpper = bp.worldAABB.upperBound

    if (flag(AABBs)) {
      val invQ = 1f / bp.quantizationFactor
      for (i <- 0 until Settings.maxProxies) {
        val p = bp.proxyPool(i)
        if (p.isValid) {
          val b = AABB(
            (worldLower.x + invQ.x * bp.m_bounds(0)(p.lowerBounds(0)).value,
             worldLower.y + invQ.y * bp.m_bounds(1)(p.lowerBounds(1)).value),
               (worldLower.x + invQ.x * bp.m_bounds(0)(p.upperBounds(0)).value,
                worldLower.y + invQ.y * bp.m_bounds(1)(p.upperBounds(1)).value))

          val vs: Array[Vector2] = Array(
            b.lowerBound,
              (b.upperBound.x, b.lowerBound.y),
              b.upperBound,
              (b.lowerBound.x, b.upperBound.y)
          )
          handler.drawPolygon(vs, Colors.AABB)
        }
      }
    }

    val vsw: Array[Vector2] = Array(
      worldLower,
      (worldUpper.x, worldLower.y),
      worldUpper,
      (worldLower.x, worldUpper.y)
    )
    handler.drawPolygon(vsw, Colors.WorldAABB)

    if (flag(CenterOfMass)) {
      for (b <- bodies) {
        val xf = Transform2(b.worldCenter, b.transform.rot)
        handler.drawTransform(xf)
      }
    }
  }

  def drawBody(body: Body) {
    val xf = body.transform
    for (f <- body.fixtures) {
      if (!f.isSensor) {
        val color = if (body.isStatic) Colors.Static
          else if (body.isSleeping) Colors.Sleeping
          else Colors.Dynamic
        drawShape(f.shape, xf, color, flag(CoreShapes))
      }
    }
  }

  def drawShape(shape: Shape, xf: Transform2, color: Color3f, core: Boolean) {
    shape match {
      case circle: Circle => 
        val center = xf * circle.pos
        val radius = circle.radius
        val axis = xf.rot.col1
        
        handler.drawSolidCircle(center, radius, axis, color)
        
        if (core) {
          handler.drawCircle(center, radius - Settings.toiSlop, Colors.Core)
        }
      case poly: Polygon =>
        val vertexCount = poly.vertexCount
        assert(vertexCount <= Settings.maxPolygonVertices)
        val localVertices = poly.vertices
        var vertices = localVertices.map(v => xf * v)

        handler.drawSolidPolygon(vertices, color)

        if (core) {
          val localCoreVertices = poly.coreVertices
          vertices = localCoreVertices.map(v => xf * v)
          handler.drawPolygon(vertices, Colors.Core)
        }
    }
  }

  def drawJoint(joint: Joint) {
    val b1 = joint.body1
    val b2 = joint.body2
    val xf1 = b1.transform
    val xf2 = b2.transform
    val x1 = xf1.pos
    val x2 = xf2.pos
    val p1 = joint.anchor1
    val p2 = joint.anchor2

    joint match {
      case _: DistanceJoint =>
        handler.drawSegment(p1, p2, Colors.Joint)
      /* if (type == JointType.PULLEY_JOINT) {
       PulleyJoint pulley = (PulleyJoint)joint;
       Vec2 s1 = pulley.getGroundAnchor1();
       Vec2 s2 = pulley.getGroundAnchor2();
       m_debughandler.drawSegment(s1, p1, color);
       m_debughandler.drawSegment(s2, p2, color);
       m_debughandler.drawSegment(s1, s2, color);
       }*/
      case _: PointingDeviceJoint =>
        //Don't draw mouse joint
      case _ =>
        handler.drawSegment(x1, p1, Colors.Joint)
        handler.drawSegment(p1, p2, Colors.Joint)
        handler.drawSegment(x2, p2, Colors.Joint)
    }
  }

}
