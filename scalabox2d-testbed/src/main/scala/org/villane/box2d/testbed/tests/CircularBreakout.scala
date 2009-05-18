package org.villane.box2d.testbed.tests

import vecmath._
import vecmath.Preamble._
import dynamics._
import shapes._

class CircularBreakout(_parent: TestbedMain) extends AbstractExample(_parent) {
	var firstTime = true
	
	def name = "Circular Breakout"

	override def createWorld() {
		m_worldAABB = AABB((-200.0f, -100.0f),(200.0f, 200.0f))
		val gravity = Vector2f(0.0f, 0.0f)
		m_world = new World(m_worldAABB, gravity, true)
	}

	override def create() {
		
		if (firstTime) {
			setCamera(0f, 20f, 20f);
			firstTime = false;
		}
		
		val k_restitution = 1.0f;

		{
			val bd = new BodyDef();
			bd.pos = (0.0f, 20.0f);
			val body = m_world.createBody(bd);

			var sd = PolygonDef.box(0.1f, 10.0f, (-10.0f, 0.0f), 0.0f);
			sd.density = 0.0f;
			sd.restitution = k_restitution;
			body.createShape(sd);

			sd = PolygonDef.box(0.1f, 10.0f, (10.0f, 0.0f), 0.0f);
			sd.density = 0.0f;
			sd.restitution = k_restitution;
			body.createShape(sd);

			sd = PolygonDef.box(0.1f, 10.0f, (0.0f, -10.0f), 0.5f * 3.1415f);
			sd.density = 0.0f;
			sd.restitution = k_restitution;
			body.createShape(sd);

			sd = PolygonDef.box(0.1f, 10.0f, (0.0f, 10.0f), -0.5f * 3.1415f);
			sd.density = 0.0f;
			sd.restitution = k_restitution;
			body.createShape(sd);
		}

		val halfCount = 3;
		val space = 1.6f;
		for (i <- -halfCount to halfCount) {
			for (j <- -halfCount to halfCount) {
				val bd = new BodyDef();
				bd.pos = (i * space, 20 + j * space);
				bd.isBullet = false
				val body = m_world.createBody(bd);
	
				val sd = new CircleDef();
				sd.radius = 0.3f;
				sd.density = 0f;
				sd.restitution = 1f;
				sd.friction = 0.0f;
				body.createShape(sd);
				body.computeMassFromShapes();
			}
		}

	}

}
