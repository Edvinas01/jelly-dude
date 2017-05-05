package com.edd.jelly.util

/**
 * Constants and helper functions for Box2D unit conversion.
 */
object Units {

    /**
     * How many pixels make up a meter.
     */
    val PPM: Float = 100f

    /**
     * How many meters make up a pixel.
     */
    val MPP: Float = 1 / PPM
}

private val ONE_RADIAN = (180 / Math.PI).toFloat()
private val ONE_DEGREE = (1 / ONE_RADIAN)

/**
 * Get number value as meters.
 */
val Number.meters: Float
    get() = toFloat() * Units.MPP

/**
 * Get number value as pixels.
 */
val Number.pixels: Float
    get() = toFloat() * Units.PPM

/**
 * Get number value converted to degrees, assuming its in radians.
 */
val Number.degrees: Float
    get() = toFloat() * ONE_RADIAN

/**
 * Get number value converted to radians, assuming its in degrees.
 */
val Number.radians: Float
    get() = toFloat() * ONE_DEGREE