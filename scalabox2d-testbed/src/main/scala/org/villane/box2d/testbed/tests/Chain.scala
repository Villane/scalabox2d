package org.villane.box2d.testbed.tests

import vecmath._
import vecmath.Preamble._
import dynamics.joints._
import dynamics._
import shapes._


class Chain(_parent: TestbedMain) extends AbstractExample(_parent) {
  var firstTime = true
  
  val name = "Chain"

  def create() {
    if (firstTime) {
      setCamera(0.0f,10.0f,10.0f);
      firstTime = false;
    }
		
    var ground: Body = null;

		{
			val bd = new BodyDef();
			bd.pos = (0.0f, -10.0f);
			ground = m_world.createBody(bd);

			val sd = PolygonDef.box(50.0f, 10.0f);
			ground.createShape(sd);
		}

		{
			val sd = PolygonDef.box(0.6f, 0.125f);
			sd.density = 20.0f;
			sd.friction = 0.2f;


			val y = 25.0f;
			var prevBody = ground;
			for (i <- 0 until 30) {
				val bd = new BodyDef();
				bd.pos = (0.5f + i, y);
				val body = m_world.createBody(bd);
				body.createShape(sd);
				body.computeMassFromShapes();
				
				val anchor = Vector2f(i, y);
				val jd = new RevoluteJointDef(prevBody, body, anchor);
        jd.collideConnected = false;
				m_world.createJoint(jd);
				
				prevBody = body;
			}
		}
	}
}