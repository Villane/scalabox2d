package org.villane.box2d.testbed.tests

import vecmath._
import vecmath.Preamble._
import dynamics.BodyDef
import shapes.PolygonDef

class Pyramid(_parent: TestbedMain) extends AbstractExample(_parent) {
  var firstTime = true

  val name = "Pyramid Stress Test"

  def create() {
    if (firstTime) {
      setCamera(2f, 12f, 10f);
      firstTime = false;
    }
    	
    	{
			val sd = PolygonDef.box(50.0f, 10.0f);

			val bd = new BodyDef();
			bd.pos = (0.0f, -10.0f);
			val ground = m_world.createBody(bd);
			ground.createShape(sd);
		}

		{
			val a = 0.5f;
			val sd = PolygonDef.box(a, a);
			sd.density = 5.0f;
			sd.restitution = 0.0f;
			sd.friction = 0.5f;

			var x = Vector2f(-10.0f, 0.75f);
			var y = Vector2f.Zero
			val deltaX = Vector2f(0.5625f, 2.0f);
			val deltaY = Vector2f(1.125f, 0.0f);

			for (i <- 0 until 25) {
				y = x

				for (j <- i until 25) {
					val bd = new BodyDef();
					bd.pos = (y);
					val body = m_world.createBody(bd);
					body.createShape(sd);
					body.computeMassFromShapes();

					y += deltaY
				}

				x += deltaX
			}
		}
    }

}
