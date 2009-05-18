package org.villane.box2d.testbed.tests

import vecmath._
import vecmath.Preamble._
import dynamics.joints._
import dynamics._
import shapes._

class Overhang(_parent: TestbedMain) extends AbstractExample(_parent) {
  val name = "Overhang"

  def create() {
        {
            val sd = PolygonDef.box(50.0f, 10.0f);

            val bd = new BodyDef();
            bd.pos = (0.0f, -10.0f);
            m_world.createBody(bd).createShape(sd);
        }

        {
            val w = 4.0f;
            val h = 0.25f;
            val sd = PolygonDef.box(w, h);
            sd.density = 1.0f;
            sd.friction = 0.3f;
            sd.restitution = 0.0f;

            val bd = new BodyDef();

            val numSlats = 8;
            var lastCMX = 0.0f;
            val eps = 0.14f;
            for (i <- 0 until numSlats) {
                val newX = lastCMX + w - eps;
                lastCMX = (i * lastCMX + newX) / (i + 1);
                bd.pos = (newX, .25f + 2 * h * (numSlats - i - 1));
                val myBody = m_world.createBody(bd);
                myBody.createShape(sd);
                myBody.computeMassFromShapes();
            }

        }
    }

}