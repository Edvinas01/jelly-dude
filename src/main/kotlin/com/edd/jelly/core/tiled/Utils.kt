package com.edd.jelly.core.tiled

import com.badlogic.gdx.maps.MapObject

/**
 * Get float value from map object properties.
 */
fun <T : MapObject> T.float(name: String, default: Float = 0f): Float {
    return this.properties.get(name)?.let {
        it as? Float ?: default
    } ?: default
}

/**
 * Get int value from map object properties.
 */
fun <T : MapObject> T.int(name: String, default: Int = 0): Int {
    return this.properties.get(name)?.let {
        it as? Int ?: default
    } ?: default
}

/**
 * Get boolean value from map object properties.
 */
fun <T : MapObject> T.boolean(name: String, default: Boolean = false): Boolean {
    return this.properties.get(name)?.let {
        it is Boolean && it
    } ?: default
}