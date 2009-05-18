package org.villane.box2d.testbed.tests

import vecmath._
import vecmath.Preamble._
import dynamics.joints._
import dynamics._
import shapes._

class Bridge(_parent: TestbedMain) extends AbstractExample(_parent) {
  var firstTime = true

  val name = "Bridge"

  def create() {
    if (firstTime) {
      setCamera(0.0f,10.0f,20.0f)
      firstTime = false
    }

    var ground: Body = null
    
		{
			val sd = PolygonDef.box(50.0f, 0.2f);

			val bd = new BodyDef
			bd.pos = (0.0f, 0.0f)
			ground = m_world.createBody(bd);
			ground.createShape(sd);
		}

		{
			val sd = PolygonDef.box(0.65f, 0.125f)
			sd.density = 20.0f;
			sd.friction = 0.2f;

			val numPlanks = 30;

			var prevBody = ground;
			for (i <- 0 until numPlanks) {
				val bd = new BodyDef();
				bd.pos = (-14.5f + 1.0f * i, 5.0f)
				val body = m_world.createBody(bd)
				body.createShape(sd);
				body.computeMassFromShapes();

				val anchor = Vector2f(-15.0f + 1.0f * i, 5.0f);
				val jd = new RevoluteJointDef(prevBody, body, anchor)
				m_world.createJoint(jd);

				prevBody = body;
			}

			val anchor = Vector2f(-15.0f + 1.0f * numPlanks, 5.0f);
			val jd = new RevoluteJointDef(prevBody, ground, anchor);
			m_world.createJoint(jd);
			
			val pd2 = PolygonDef.box(1.0f,1.0f);
			pd2.density = 5.0f;
			pd2.friction = 0.2f;
			pd2.restitution = 0.1f;
			val bd2 = new BodyDef();
			bd2.pos = (0.0f, 10.0f);
			val body2 = m_world.createBody(bd2);
			body2.createShape(pd2);
			body2.computeMassFromShapes();
			
			val cd = new CircleDef();
			cd.radius = 0.9f;
			cd.density = 5.0f;
			cd.friction = 0.2f;
			val bd3 = new BodyDef();
			bd3.pos = (0.0f, 12.0f);
			val body3 = m_world.createBody(bd3);
			body3.createShape(cd);
			cd.pos = (0.0f,1.0f);
			body3.createShape(cd);
			body3.computeMassFromShapes();
		}
    }

}
