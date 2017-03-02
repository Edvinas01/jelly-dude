package com.edd.jelly.core.tiled

import com.badlogic.gdx.maps.MapLayer
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.edd.jelly.exception.GameException
import com.edd.jelly.util.meters
import com.edd.jelly.util.resources.ResourceManager
import com.google.inject.Inject

class JellyMapLoader @Inject constructor(
        private val resourceManager: ResourceManager,
        private val tmxMapLoader: TmxMapLoader
) {

    private companion object {
        val LEVEL_DIRECTORY = "levels"
        val LEVEL_FILE_TYPE = "tmx"

        val BACKGROUND_TEXTURE = "background_texture"
        val ENTITY_LAYER = "entities"
    }

    /**
     * Load jelly map by name.
     */
    fun loadMap(name: String): JellyMap {
        val map = getTiledMap(name)

        // Required main layer.
        if (map.layers[ENTITY_LAYER] == null) {
            throw GameException("Layer \"$ENTITY_LAYER\" does not exist")
        }

        var background = true
        val backgroundLayers = mutableListOf<MapLayer>()
        val foregroundLayers = mutableListOf<MapLayer>()

        for (layer in map.layers
                // Top to bottom, dunno why they're revered.
                .reversed()) {

            background = background && ENTITY_LAYER != layer.name

            // Care only about renderable layers.
            if (layer !is TiledMapTileLayer) {
                continue
            }

            // Entity layer should be in background layers just in-case someone made it a tile layer - looks nicer.
            if (ENTITY_LAYER == layer.name) {
                backgroundLayers.add(layer)
                continue
            }

            (if (background) backgroundLayers else foregroundLayers).apply {
                if (layer.getBoolean("parallax")) {
                    add(ParallaxLayer(
                            layer.getFloat("offsetX").meters,
                            layer.getFloat("offsetY").meters,
                            layer.getFloat("speedX", 1f),
                            layer.getFloat("speedY", 1f),
                            resourceManager.getTexture(layer.getString("texture") ?:
                                    throw GameException("Please specify parallax \"texture\" name"))
                    ))
                } else {
                    add(layer)
                }
            }
        }
        return JellyMap(
                map,
                backgroundLayers,
                foregroundLayers,
                map.properties.get(BACKGROUND_TEXTURE)?.let {
                    resourceManager.getTexture(it as String)
                }
        )
    }

    /**
     * Get tiled map by name.
     */
    private fun getTiledMap(name: String): TiledMap {
        return tmxMapLoader.load("$LEVEL_DIRECTORY/$name/$name.$LEVEL_FILE_TYPE")
    }

    /**
     * Get string value from map layer properties.
     */
    private fun <T : MapLayer> T.getString(name: String): String? {
        return this.properties.get(name)?.let {
            it as String
        } ?: null
    }

    /**
     * Get float value from map layer properties.
     */
    private fun <T : MapLayer> T.getFloat(name: String, default: Float = 0f): Float {
        return this.properties.get(name)?.let {
            if (it is Float) it else default
        } ?: default
    }

    /**
     * Get boolean value from map layer properties.
     */
    private fun <T : MapLayer> T.getBoolean(name: String, default: Boolean = false): Boolean {
        return this.properties.get(name)?.let {
            it is Boolean && it
        } ?: default
    }
}