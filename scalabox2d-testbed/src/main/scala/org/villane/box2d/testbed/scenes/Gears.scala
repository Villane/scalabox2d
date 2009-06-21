package org.villane.box2d.testbed.scenes

import vecmath._
import vecmath.Preamble._
import dsl.DSL._
import shapes._

object Gears extends TestbedScene {

  def createScene(implicit world: dynamics.World) {
   val ground = body {
     pos(0.0f, -10.0f)
     box(50.0f, 10.0f)
   }

   val circle1 = circle(1) density 5 define
   val r1 = circle1.shapeDef.asInstanceOf[CircleDef].radius
   val circle2 = circle(2) density 5 define
   val r2 = circle1.shapeDef.asInstanceOf[CircleDef].radius

   val box1 = box(0.5f, 5.0f) density 5

   val body1 = body {
     pos(-3.0f, 12.0f)
     fixture(circle1)
     massFromShapes
   }

   val rev1 = joint(
     revolute(ground -> body1) anchor(body1.pos)
   )

   val body2 = body {
     pos(0.0f, 12.0f)
     fixture(circle2)
     massFromShapes
   }

   val rev2 = joint(
     revolute(ground -> body2) anchor body2.pos
   )

   val body3 = body {
     pos(2.5f, 12.0f)
     fixture(box1)
     massFromShapes
   }

   val prism = joint(
     prismatic(ground -> body3)
       anchor(body3.pos)
       axis(Vector2.YUnit)
       lowerTranslation -5.0f
       upperTranslation 5.0f
       enableLimit true
   )

   joint(
     gear(rev1 -> rev2) ratio(r2 / r1)
   )

   joint(
     gear(rev2 -> prism) ratio(-1 / r2)
   )
 }

}
