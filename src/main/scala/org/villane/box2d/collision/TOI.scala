package org.villane.box2d.collision

import dynamics.Sweep
import vecmath._
import vecmath.Preamble._
import shapes._

import settings.Settings
import settings.Settings.ε

/** Handles conservative advancement to compute time of impact between shapes. */
object TOI {
  // This algorithm uses conservative advancement to compute the time of
  // impact (TOI) of two shapes.
  // Refs: Bullet, Young Kim
	
  /**
   * Compute the time when two shapes begin to touch or touch at a closer distance.
   * <BR><BR><em>Warning</em>: the sweeps must have the same time interval.
   * @return the fraction between [0,1] in which the shapes first touch.
   * fraction=0 means the shapes begin touching/overlapped, and fraction=1 means the shapes don't touch.
   */
  def timeOfImpact(shape1: Shape, sweep1: Sweep,
                   shape2: Shape, sweep2: Sweep) = {
    val r1 = shape1.sweepRadius
    val r2 = shape2.sweepRadius

    assert(sweep1.t0 == sweep2.t0)
    assert(1.0f - sweep1.t0 > ε)

    val t0 = sweep1.t0
    val v1 = sweep1.c - sweep1.c0
    val v2 = sweep2.c - sweep2.c0
    val ω1 = sweep1.a - sweep1.a0
    val ω2 = sweep2.a - sweep2.a0

    var α = 0f

    val k_maxIterations = 20 // TODO_ERIN b2Settings
    var iter = 0
    var targetDistance = 0f
    // use loop = false instead of breaks as 'break' isn't available in Scala
    var loop = true
    while(loop) {
      val t = (1f - α) * t0 + α
      val xf1 = sweep1.getTransform(t)
      val xf2 = sweep2.getTransform(t)

      // Get the distance between shapes.
      val (distance,p1,p2) = Distance.distance(shape1, xf1, shape2, xf2)
      //System.out.println(distance);

      if (iter == 0) {
        // Compute a reasonable target distance to give some breathing room
        // for conservative advancement.
        if (distance > 2.0f * Settings.toiSlop) {
          targetDistance = 1.5f * Settings.toiSlop;
        } else {
          targetDistance = MathUtil.max(0.05f * Settings.toiSlop, distance - 0.5f * Settings.toiSlop);
        }
      }

      if (distance - targetDistance < 0.05f * Settings.toiSlop || iter == k_maxIterations) {
        //if (distance-targetDistance < 0) System.out.println("dist error: "+ (distance-targetDistance) + " toiSlop: "+Settings.toiSlop + " iter: "+iter);
        loop = false
      } else {
        val normal = (p2 - p1).normalize

        // Compute upper bound on remaining movement.
        val approachVelocityBound = (normal ∙ (v1 - v2)) + ω1.abs * r1 + ω2.abs * r2
        if (approachVelocityBound.abs < ε) {
          α = 1.0f
          loop = false
        } else {
          // Get the conservative time increment. Don't advance all the way.
          val dAlpha = (distance - targetDistance) / approachVelocityBound
          //float32 dt = (distance - 0.5f * b2_linearSlop) / approachVelocityBound;
          val newAlpha = α + dAlpha

          // The shapes may be moving apart or a safe distance apart.
          if (newAlpha < 0.0f || 1.0f < newAlpha) {
            α = 1.0f
            loop = false
          } else {

            // Ensure significant advancement.
            if (newAlpha < (1.0f + 100.0f * ε) * α) {
              loop = false
            } else {
              α = newAlpha
              iter += 1
            }
          }
        }
      }
    }

    α
  }
}
