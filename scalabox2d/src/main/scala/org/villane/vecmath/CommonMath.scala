package org.villane.vecmath

/**
 * Math functions not differing for different Floating Point types.
 */
trait CommonMath {

  final def distance(a: Vector2, b: Vector2) = (a - b).length

  final def distanceSquared(a: Vector2, b: Vector2) = {
    val d = a - b
    d dot d
  }

}
