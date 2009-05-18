package org.villane.box2d.testbed.tests

import vecmath._
import vecmath.Preamble._
import dynamics.joints._
import dynamics._
import shapes._
import MathUtil.π

class Circles(_parent: TestbedMain) extends AbstractExample(_parent) {
  var firstTime = true

  val name = "Circle Stress Test"

  def create() {
    	if (firstTime) {
			setCamera(0f, 20f, 5f);
			firstTime = false;
		}
    	
        val ground = m_world.groundBody
        var leftWall: Body = null;
        var rightWall: Body = null;
        {
            // Ground
            var sd = PolygonDef.box(50.0f, 10.0f);
            sd.friction = 1.0f;
            var bd = new BodyDef();
            bd.pos = (0.0f, -10.0f);
            m_world.createBody(bd).createShape(sd);
            
            
            // Walls
            sd = PolygonDef.box(3.0f,50.0f);
            sd.friction = 1.0f;
            bd = new BodyDef();
            bd.pos = (53.0f,25.0f);
            rightWall = m_world.createBody(bd);
            rightWall.createShape(sd);
            bd.pos = (-53.0f,25.0f);
            leftWall = m_world.createBody(bd);
            leftWall.createShape(sd);
            
            // Corners 
            bd = new BodyDef();
            sd = PolygonDef.box(20.0f,3.0f);
            sd.friction = 1.0f;
            bd.angle = -π/4
            bd.pos = (-40f,0.0f);
            var myBod = m_world.createBody(bd);
            myBod.createShape(sd);
            bd.angle = π/4
            bd.pos = (40f,0.0f);
            myBod = m_world.createBody(bd);
            myBod.createShape(sd);
            
        }
        
        val bd = new BodyDef();
        val numPieces = 5;
        val radius = 6f;
        bd.pos = (0.0f,10.0f);
        val body = m_world.createBody(bd);
        for (i <- 0 until numPieces) {
            val cd = new CircleDef();
            cd.radius = 1.2f;
            cd.density = 25.0f;
            cd.friction = 0.1f;
            cd.restitution = 0.9f;
            val xPos = radius * MathUtil.cos(2*π * (i.toFloat / (numPieces)))
            val yPos = radius * MathUtil.sin(2*π * (i.toFloat / (numPieces)))
            cd.pos = (xPos,yPos);
            body.createShape(cd);   
        }
        body.computeMassFromShapes();

        val rjd = new RevoluteJointDef(body,ground,body.pos);
        rjd.motorSpeed = π
        rjd.maxMotorTorque = 1000000.0f;
        rjd.enableMotor = true;
        m_world.createJoint(rjd);
        
        {
            val loadSize = 45;

            for (j <- 0 until 10){
                for (i <- 0 until loadSize) {
                    val circ = new CircleDef();
                    val bod = new BodyDef();
                    circ.radius = 1.0f+(if (i%2==0) 1.0f else -1.0f)*.5f*(i.toFloat/loadSize);
                    circ.density = 5.0f;
                    circ.friction = 0.1f;
                    circ.restitution = 0.5f;
                    val xPos = -45f + 2*i;
                    val yPos = 50f+j;
                    bod.pos = (xPos,yPos);
                    val myBody = m_world.createBody(bod);
                    myBody.createShape(circ);
                    myBody.computeMassFromShapes();
                    
                }
            }
            
            
        }

    }

}