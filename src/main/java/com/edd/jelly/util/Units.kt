package com.edd.jelly.util

/**
 * Constants and helper functions for Box2D unit conversion.
 */
object Units {

    /**
     * How many pixels make up a meter.
     */
    val PPM: Float = 50f

    /**
     * How many meters make up a pixel.
     */
    val MPP: Float = 1 / PPM
}

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