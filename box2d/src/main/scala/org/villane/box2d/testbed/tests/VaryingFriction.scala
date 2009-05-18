package org.villane.box2d.testbed.tests

import vecmath._
import vecmath.Preamble._
import dynamics.joints._
import dynamics._
import shapes._

class VaryingFriction(_parent: TestbedMain) extends AbstractExample(_parent) {
  val name = "Varying Friction"

  def create() {
		{
			val sd = PolygonDef.box(100.0f, 20.0f);

			val bd = new BodyDef();
			bd.pos = (0.0f, -20.0f);
			val ground = m_world.createBody(bd);
			ground.createShape(sd);
		}

		{
			val sd = PolygonDef.box(13.0f, 0.25f);

			val bd = new BodyDef();
			bd.pos = (-4.0f, 22.0f);
			bd.angle = -0.25f;

			val ground = m_world.createBody(bd);
			ground.createShape(sd);
		}

		{
			val sd = PolygonDef.box(0.25f, 1.0f);

			val bd = new BodyDef();
			bd.pos = (10.5f, 19.0f);

			val ground = m_world.createBody(bd);
			ground.createShape(sd);
		}

		{
			val sd = PolygonDef.box(13.0f, 0.25f);

			val bd = new BodyDef();
			bd.pos = (4.0f, 14.0f);
			bd.angle = 0.25f;

			val ground = m_world.createBody(bd);
			ground.createShape(sd);
		}

		{
			val sd = PolygonDef.box(0.25f, 1.0f);

			val bd = new BodyDef();
			bd.pos = (-10.5f, 11.0f);

			val ground = m_world.createBody(bd);
			ground.createShape(sd);
		}

		{
			val sd = PolygonDef.box(13.0f, 0.25f);

			val bd = new BodyDef();
			bd.pos = (-4.0f, 6.0f);
			bd.angle = -0.25f;

			val ground = m_world.createBody(bd);
			ground.createShape(sd);
		}

		{
			val sd = PolygonDef.box(0.5f, 0.5f);
			sd.density = 25.0f;

			val friction = Array(0.75f, 0.5f, 0.35f, 0.1f, 0.0f)

			for (i <- 0 until 5)
			{
				val bd = new BodyDef();
				bd.pos = (-15.0f + 4.0f * i, 28.0f);
				val body = m_world.createBody(bd);

				sd.friction = friction(i);
				body.createShape(sd);
				body.computeMassFromShapes();
			}
		}
	}

}
