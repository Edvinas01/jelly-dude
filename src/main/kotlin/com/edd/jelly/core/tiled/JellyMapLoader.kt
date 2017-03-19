package com.edd.jelly.core.tiled

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.maps.MapLayer
import com.badlogic.gdx.maps.objects.EllipseMapObject
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.badlogic.gdx.math.Vector2
import com.edd.jelly.exception.GameException
import com.edd.jelly.util.meters
import com.edd.jelly.core.resources.ResourceManager
import com.google.inject.Inject
import com.google.inject.Singleton

@Singleton
class JellyMapLoader @Inject constructor(
        private val resourceManager: ResourceManager,
        private val tmxMapLoader: TmxMapLoader,
        private val camera: OrthographicCamera
) {

    private enum class LayerType {
        PARALLAX,
        DEFAULT
    }

    private companion object {
        val LEVEL_DIRECTORY = "levels"
        val LEVEL_FILE_TYPE = "tmx"

        val BACKGROUND_TEXTURE = "background_texture"
        val ENTITY_LAYER = "entities"
        val SPAWN_NAME = "start"
    }

    /**
     * Load jelly map by name.
     */
    fun loadMap(name: String): JellyMap {
        val map = getTiledMap(name)

        // Required main layer.
        val entities: MapLayer = map.layers[ENTITY_LAYER]
                ?: throw GameException("Layer \"$ENTITY_LAYER\" does not exist")

        var background = true
        val backgroundLayers = mutableListOf<MapLayer>()
        val foregroundLayers = mutableListOf<MapLayer>()


        for (layer in map.layers) {
            background = background && ENTITY_LAYER != layer.name

            // Care only about renderable and visible layers.
            if (layer !is TiledMapTileLayer || !layer.isVisible) {
                continue
            }

            if (ENTITY_LAYER == layer.name) {
                continue
            }

            (if (background) backgroundLayers else foregroundLayers).apply {

                when (layer.getString("type")?.let {
                    LayerType.valueOf(it)
                } ?: LayerType.DEFAULT) {
                    LayerType.PARALLAX -> {
                        val texture = resourceManager.getTexture(layer.getString("texture") ?:
                                throw GameException("Please specify parallax \"texture\" name"))

                        add(ParallaxLayer(
                                Vector2(
                                        layer.getFloat("offsetX").meters,
                                        layer.getFloat("offsetY").meters
                                ),
                                Vector2(
                                        layer.getFloat("speedX", 1f),
                                        layer.getFloat("speedY", 1f)
                                ),
                                layer.getBoolean("clampTop", false),
                                layer.getBoolean("clampBottom", false),
                                texture,
                                Vector2(
                                        (if (layer.getBoolean("fitX"))
                                            camera.viewportWidth
                                        else texture.width.meters) * layer.getFloat("scaleX", 1f),
                                        (if (layer.getBoolean("fitY"))
                                            camera.viewportHeight
                                        else texture.height.meters) * layer.getFloat("scaleY", 1f)
                                )
                        ))
                    }
                    else -> {
                        add(layer)
                    }
                }
            }
        }
        return JellyMap(
                map,
                backgroundLayers,
                foregroundLayers,
                map.properties.get(BACKGROUND_TEXTURE)?.let {
                    resourceManager.getTexture(it as String)
                },
                getSpawn(entities)
        )
    }

    /**
     * Get spawn point from entities layer.
     */
    private fun getSpawn(entities: MapLayer): Vector2 {
        return entities.objects.find {
            it is EllipseMapObject && SPAWN_NAME == it.name
        }?.let {
            val ellipse = (it as EllipseMapObject).ellipse
            Vector2(ellipse.x, ellipse.y)
        } ?: throw GameException("$SPAWN_NAME circle object must exist somewhere in the map")
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