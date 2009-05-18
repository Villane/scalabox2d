package org.villane.box2d.testbed.tests

import vecmath._
import vecmath.Preamble._
import dynamics.BodyDef
import shapes.PolygonDef

class Domino(_parent: TestbedMain) extends AbstractExample(_parent) {
  val name = "Domino Test"
  def create() {
        { // Floor
            val sd = PolygonDef.box(50.0f, 10.0f);

            val bd = new BodyDef();
            bd.pos = (0.0f, -10.0f)
            m_world.createBody(bd).createShape(sd);
            
        }

        { // Platforms
            for (i <- 0 to 3) {
            	val sd = PolygonDef.box(15.0f, 0.125f);

                val bd = new BodyDef();
                bd.pos = (0.0f, 5f + 5f * i)
                m_world.createBody(bd).createShape(sd);
            }
        }

        {
        	val sd = PolygonDef.box(0.125f, 2f)
            sd.density = 25.0f;

            val bd = new BodyDef();
            
            var friction = .5f;
            var numPerRow = 25;

            for (i <- 0 to 3) {
                for (j <- 0 until numPerRow) {
                    sd.friction = friction;
                    bd.pos = (-14.75f + j
                            * (29.5f / (numPerRow - 1)), 7.3f + 5f * i);
                    if (i == 2 && j == 0) {
                        bd.angle = -0.1f;
                        bd.pos += (.1f,0);
                    }
                    else if (i == 3 && j == numPerRow - 1) {
                        bd.angle = .1f;
                        bd.pos -= (.1f,0);
                    }
                    else
                        bd.angle = 0f;
                    val myBody = m_world.createBody(bd);
                    myBody.createShape(sd);
                    myBody.computeMassFromShapes();
                }
            }
        }
    }

}
