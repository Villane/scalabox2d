package org.villane.box2d.draw

import collision._
import vecmath._
import vecmath.Preamble._
import shapes._
import settings.Settings
import dynamics._
import dynamics.joints._

class DebugDraw(val draw: DebugDrawHandler) {

  val OBB_COLOR = Color3f(255.0f * 0.5f, 255.0f * 0.3f, 255.0f * 0.5f)
  val STATIC_COLOR = Color3f(255f*0.5f, 255f*0.9f, 255f*0.5f)
  val SLEEPING_COLOR = Color3f(255f*0.5f, 255f*0.5f, 255f*0.9f)
  val DYNAMIC_COLOR = Color3f(255f*0.9f, 255f*0.9f, 255f*0.9f)
  val PAIR_COLOR = Color3f(255f*0.9f, 255f*0.9f, 255f*0.3f)
  val AABB_COLOR = Color3f(255.0f * 0.3f, 255.0f * 0.9f, 255.0f * 0.9f)
  val CORE_COLOR = Color3f(255f * 0.9f, 255f * 0.6f, 255f * 0.6f)

  /** For internal use */
  def drawShape(shape: Shape, xf: Transform2f, color: Color3f, core: Boolean) {
    
    val coreColor = CORE_COLOR

    shape match {
      case circle: Circle => 
        val center = xf * circle.pos
        val radius = circle.radius
        val axis = xf.rot.col1
        
        draw.drawSolidCircle(center, radius, axis, color)
        
        if (core) {
          draw.drawCircle(center, radius - Settings.toiSlop, coreColor)
        }
      case poly: Polygon =>
        val vertexCount = poly.vertexCount
        assert(vertexCount <= Settings.maxPolygonVertices)
        val localVertices = poly.vertices
        var vertices = localVertices.map(v => xf * v)

        draw.drawSolidPolygon(vertices, color)

        if (core) {
          val localCoreVertices = poly.coreVertices
          vertices = localCoreVertices.map(v => xf * v)
          draw.drawPolygon(vertices, coreColor)
        }
    }
  }

  /** For internal use */
  def drawJoint(joint: Joint) {
    val b1 = joint.body1
    val b2 = joint.body2
    val xf1 = b1.transform
    val xf2 = b2.transform
    val x1 = xf1.pos
    val x2 = xf2.pos
    val p1 = joint.anchor1
    val p2 = joint.anchor2

    val color = new Color3f(255f*0.5f, 255f*0.8f, 255f*0.8f);

    joint match {
      /*if (type == JointType.DISTANCE_JOINT) {
       m_debugDraw.drawSegment(p1, p2, color);
       } else if (type == JointType.PULLEY_JOINT) {
       PulleyJoint pulley = (PulleyJoint)joint;
       Vec2 s1 = pulley.getGroundAnchor1();
       Vec2 s2 = pulley.getGroundAnchor2();
       m_debugDraw.drawSegment(s1, p1, color);
       m_debugDraw.drawSegment(s2, p2, color);
       m_debugDraw.drawSegment(s1, s2, color);
       }*/
      case _: PointingDeviceJoint =>
        //Don't draw mouse joint
      case _ =>
        draw.drawSegment(x1, p1, color)
        draw.drawSegment(p1, p2, color)
        draw.drawSegment(x2, p2, color)
    }
  }

  /** For internal use */
  def drawDebugData(bodies: Seq[Body], joints: Seq[Joint], bp: BroadPhase) {
    
    val flags = draw.drawFlags

    if ((flags & DrawFlags.shape) != 0) {
      val core = (flags & DrawFlags.coreShape) == DrawFlags.coreShape
      for (b <- bodies) {
        val xf = b.transform
        for (s <- b.shapes) {
          if (!s.isSensor) {
            val color = if (b.isStatic)
            STATIC_COLOR
            else if (b.isSleeping)
            SLEEPING_COLOR
            else
            DYNAMIC_COLOR
            drawShape(s, xf, color, core)
          }
        }
      }
    }

    if ((flags & DrawFlags.joint) != 0) {
      joints.foreach(drawJoint)
    }

    if ((flags & DrawFlags.pair) != 0) {
      val invQ = 1f / bp.quantizationFactor

      val color = PAIR_COLOR

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

        draw.drawSegment(x1, x2, color)
      }
    }

    val worldLower = bp.worldAABB.lowerBound
    val worldUpper = bp.worldAABB.upperBound

    if ((flags & DrawFlags.aabb) != 0) {
      val invQ = 1f / bp.quantizationFactor
      val color = Color3f(255f, 255f, 255f)
      for (i <- 0 until Settings.maxProxies) {
        val p = bp.proxyPool(i)
        if (p.isValid) {
          val b = AABB(
            (worldLower.x + invQ.x * bp.m_bounds(0)(p.lowerBounds(0)).value,
             worldLower.y + invQ.y * bp.m_bounds(1)(p.lowerBounds(1)).value),
               (worldLower.x + invQ.x * bp.m_bounds(0)(p.upperBounds(0)).value,
                worldLower.y + invQ.y * bp.m_bounds(1)(p.upperBounds(1)).value))

          val vs: Array[Vector2f] = Array(
            b.lowerBound,
              (b.upperBound.x, b.lowerBound.y),
              b.upperBound,
              (b.lowerBound.x, b.upperBound.y)
          )
          draw.drawPolygon(vs, color)
        }
      }
    }

    val vsw: Array[Vector2f] = Array(
      worldLower,
        (worldUpper.x, worldLower.y),
        worldUpper,
        (worldLower.x, worldUpper.y)
    )
    draw.drawPolygon(vsw, AABB_COLOR)

    if ((flags & DrawFlags.obb) != 0) {

      val color = OBB_COLOR

      for (b <- bodies) {
        val xf = b.transform
        for (s <- b.shapes) {
          if (s.isInstanceOf[Polygon]) {
            val poly = s.asInstanceOf[Polygon]
            val obb = poly.obb
            val h = obb.extents
            val vs: Array[Vector2f] = Array(
              -h,
                (h.x, -h.y),
                h,
                (-h.x, h.y)
            )

            for (i <- 0 until 4) {
              vs(i) = obb.center + (obb.rot * vs(i))
              vs(i) = xf * vs(i)
            }
            draw.drawPolygon(vs, color)
          }
        }
      }
    }

    if ((flags & DrawFlags.centerOfMass) != 0) {
      for (b <- bodies) {
        val xf = Transform2f(b.worldCenter, b.transform.rot)
        draw.drawTransform(xf)
      }
    }
  }
}