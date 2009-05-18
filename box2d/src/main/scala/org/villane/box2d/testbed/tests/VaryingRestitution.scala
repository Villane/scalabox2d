package org.villane.box2d.testbed.tests

import vecmath._
import vecmath.Preamble._
import dynamics.joints._
import dynamics._
import shapes._

class VaryingRestitution(_parent: TestbedMain) extends AbstractExample(_parent) {

  val name = "Varying Restitution"

  def create() {
        {
            val sd = PolygonDef.box(50.0f, 10.0f);
            
            val bd = new BodyDef();
            bd.pos = (0.0f, -10.0f);
            m_world.createBody(bd).createShape(sd);
        }

        {
            val sd = new CircleDef();
            sd.radius = .6f;
            sd.density = 5.0f;

            val bd = new BodyDef();
            

            val restitution = Array( 0.0f, 0.1f, 0.3f, 0.5f, 0.75f,
                    0.9f, 1.0f );

            for (i <- 0 until restitution.length) {
                sd.restitution = restitution(i)
                bd.pos = (-10.0f + 3.0f * i, 10.0f);
                val myBody = m_world.createBody(bd);
                myBody.createShape(sd);
                myBody.computeMassFromShapes();
            }
        }
    }
}
