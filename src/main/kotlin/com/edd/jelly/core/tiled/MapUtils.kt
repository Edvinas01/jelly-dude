package com.edd.jelly.core.tiled

import com.badlogic.gdx.maps.MapLayer
import com.badlogic.gdx.maps.MapObject
import com.badlogic.gdx.maps.tiled.TiledMap
import com.edd.jelly.util.GameException

/**
 * Get tiled map layer or throw an exception.
 */
fun <T : TiledMap> T.mustLayer(name: String): MapLayer {
    return this.layers[name]
            ?: throw GameException("Layer \"$name\" does not exist")
}

/**
 * Get tiled map int property or throw an exception.
 */
fun <T : TiledMap> T.int(name: String): Int {
    return this.properties.get(name)?.let {
        it as? Int ?: 0
    } ?: 0
}

/**
 * Get string value from map object properties.
 */
fun <T : MapObject> T.string(name: String): String? {
    return this.properties.get(name)?.let {
        it as? String
    }
}

/**
 * Get string value from map object properties.
 */
fun <T : MapObject> T.string(name: String, default: String = ""): String {
    return this.string(name) ?: default
}

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

/**
 * Get string value from map layer properties.
 */
fun <T : MapLayer> T.string(name: String): String? {
    return this.properties.get(name)?.let {
        it as String
    }
}

/**
 * Get float value from map layer properties.
 */
fun <T : MapLayer> T.float(name: String, default: Float = 0f): Float {
    return this.properties.get(name)?.let {
        it as? Float ?: default
    } ?: default
}

/**
 * Get boolean value from map layer properties.
 */
fun <T : MapLayer> T.boolean(name: String, default: Boolean = false): Boolean {
    return this.properties.get(name)?.let {
        it is Boolean && it
    } ?: default
}