package org.villane.box2d.testbed.tests

import vecmath._
import vecmath.Preamble._
import dynamics.joints._
import dynamics._
import shapes._

class CCDTest(_parent: TestbedMain) extends AbstractExample(_parent) {
  var firstTime = true
  val name = "Continuous Collision Test"
	
  def create() {
		
		if (firstTime) {
			setCamera(0f, 20f, 20f);
			firstTime = false;
		}
		
		val k_restitution = 1.4f;

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

			sd = PolygonDef.box(0.1f, 10.0f,(0.0f, -10.0f), 0.5f * 3.1415f);
			sd.density = 0.0f;
			sd.restitution = k_restitution;
			body.createShape(sd);

			sd = PolygonDef.box(0.1f, 10.0f, (0.0f, 10.0f), -0.5f * 3.1415f);
			sd.density = 0.0f;
			sd.restitution = k_restitution;
			body.createShape(sd);
		}

		{
			val sd_bottom = PolygonDef.box( 1.5f, 0.15f );
			sd_bottom.density = 4.0f;

			val sd_left = PolygonDef.box(0.15f, 2.7f, (-1.45f, 2.35f), 0.2f);
			sd_left.density = 4.0f;

			val sd_right = PolygonDef.box(0.15f, 2.7f, (1.45f, 2.35f), -0.2f);
			sd_right.density = 4.0f;

			val bd = new BodyDef();
			bd.pos = ( 0.0f, 15.0f );
			bd.isBullet = true;
			val body = m_world.createBody(bd);
			body.createShape(sd_bottom);
			body.createShape(sd_left);
			body.createShape(sd_right);
			body.computeMassFromShapes();
		}


		for (i <- 0 until 10) {
			val bd = new BodyDef();
			bd.pos = (0.0f, 15.5f + i);
			bd.isBullet = true;
			val body = m_world.createBody(bd);
			body.angularVelocity = new util.Random().nextFloat * 100 - 50 //(-50.0f, 50.0f)

			val sd = new CircleDef();
			sd.radius = 0.25f;
			sd.density = 1.0f;
			sd.restitution = 0.0f;
			sd.friction = 0.05f;
			body.createShape(sd);
			body.computeMassFromShapes();
		}

	}
}
