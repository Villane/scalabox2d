package org.villane.box2d

import vecmath.MathUtil.π

/** Global tuning constants based on MKS units and various integer maximums (vertices per shape, pairs, etc.). */
object Settings {

  /** A "close to zero" float epsilon value for use */
  val ε = 1.1920928955078125E-7f
  val Epsilon = ε

  // Define your unit system here. The default system is
  // meters-kilograms-seconds. For the tuning to work well,
  // your dynamic objects should be bigger than a pebble and smaller
  // than a house.
  //
  // Use of these settings has been deprecated - they do not even
  // exist anymore in the C++ version of the engine, and future support
  // is unlikely.
  val lengthUnitsPerMeter = 1.0f
  val massUnitsPerKilogram = 1.0f
  val timeUnitsPerSecond = 1.0f

  // Collision

  val maxManifoldPoints = 2
  val maxShapesPerBody = 64
  val maxPolygonVertices = 8

  // ERKKI XXX do these still have to be power of two?
  /** Must be a power of two. */
  val maxProxies = 2048
  /** Must be a power of two. */
  val maxPairs = 8 * maxProxies

  // Dynamics

  /**
   * A small length used as a collision and constraint tolerance. Usually it is
   * chosen to be numerically significant, but visually insignificant.
   */
  val linearSlop = 0.005f * lengthUnitsPerMeter; // 0.5 cm

  /**
   * A small angle used as a collision and constraint tolerance. Usually it is
   * chosen to be numerically significant, but visually insignificant.
   */
  val angularSlop = 2.0f / 180.0f * π // 2 degrees

  /**
   * The radius of the polygon/edge shape skin. This should not be modified.
   * Making this smaller means polygons will have and insufficient for
   * continuous collision. Making it larger may create artifacts for vertex
   * collision.
   */
  val polygonRadius = 2.0f * linearSlop

  /**
   * A velocity threshold for elastic collisions. Any collision with a relative linear
   * velocity below this threshold will be treated as inelastic.
   */
  val velocityThreshold = 1.0f * lengthUnitsPerMeter / timeUnitsPerSecond // 1 m/s

  /**
   * The maximum linear position correction used when solving constraints. This helps to
   * prevent overshoot.
   */
  val maxLinearCorrection = 0.2f * lengthUnitsPerMeter // 20 cm

  /**
   * The maximum angular position correction used when solving constraints. This helps to
   * prevent overshoot.
   */
  val maxAngularCorrection = 8.0f / 180.0f * π // 8 degrees

  /**
   * This scale factor controls how fast overlap is resolved. Ideally this would be 1 so
   * that overlap is removed in one time step. However using values close to 1 often lead
   * to overshoot.
   */
  val contactBaumgarte = 0.2f

  /** The time that a body must be still before it will go to sleep. */
  val timeToSleep = 0.5f * timeUnitsPerSecond // half a second

  /** A body cannot sleep if its linear velocity is above this tolerance. */
  val linearSleepTolerance = 0.01f * lengthUnitsPerMeter / timeUnitsPerSecond // 1 cm/s

  /** A body cannot sleep if its angular velocity is above this tolerance. */
  val angularSleepTolerance = 2.0f / 180.0f / timeUnitsPerSecond
    
  /**
   * Continuous collision detection (CCD) works with core, shrunken shapes. This is the
   * amount by which shapes are automatically shrunk to work with CCD. This must be
   * larger than b2_linearSlop.
   */
  val toiSlop = 8.0f * linearSlop

  /**
   * The maximum linear velocity of a body. This limit is very large and is used
   * to prevent numerical problems. You shouldn't need to adjust this.
   */
  val maxLinearVelocity = 200.0f
  val maxLinearVelocitySquared = maxLinearVelocity * maxLinearVelocity

  /**
   * The maximum angular velocity of a body. This limit is very large and is used
   * to prevent numerical problems. You shouldn't need to adjust this.
   */
  val maxAngularVelocity = 250.0f
  val maxAngularVelocitySquared = maxAngularVelocity * maxAngularVelocity

  /** Maximum number of contacts to be handled to solve a TOI island. */
  val maxTOIContactsPerIsland = 32

  var threadedIslandSolving = true
  var numThreads = 3//Runtime.getRuntime.availableProcessors
}
