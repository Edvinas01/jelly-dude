package com.edd.jelly.core.tiled

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.maps.MapLayer
import com.badlogic.gdx.maps.MapObject
import com.badlogic.gdx.maps.objects.CircleMapObject
import com.badlogic.gdx.maps.objects.EllipseMapObject
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.badlogic.gdx.math.Vector2
import com.edd.jelly.core.InternalMapLoader
import com.edd.jelly.core.configuration.Configurations
import com.edd.jelly.exception.GameException
import com.edd.jelly.util.meters
import com.edd.jelly.core.resources.ResourceManager
import com.google.inject.Inject
import com.google.inject.Singleton
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.stream.Collectors

@Singleton
class JellyMapLoader @Inject constructor(
        private val resourceManager: ResourceManager,

        @InternalMapLoader
        private val internalTmxMapLoader: TmxMapLoader,
        private val tmxMapLoader: TmxMapLoader,
        private val camera: OrthographicCamera
) {

    val metadata = loadMeta()

    private enum class LayerType {
        PARALLAX,
        DEFAULT
    }

    private companion object {
        const val LEVEL_DIRECTORY = "levels/"
        const val LEVEL_FILE_TYPE = "tmx"

        const val BACKGROUND_TEXTURE = "background_texture"
        const val ENTITY_LAYER = "entities"
        const val SPAWN_NAME = "start"
    }

    /**
     * Load jelly map by name.
     */
    fun loadMap(name: String, internal: Boolean = false): JellyMap {
        val map = getTiledMap(name, internal)

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
                name,
                map,
                backgroundLayers,
                foregroundLayers,
                map.properties.get(BACKGROUND_TEXTURE)?.let {
                    resourceManager.getTexture(it as String)
                },
                getSpawn(entities),
                getFocusPoints(entities)
        )
    }

    /**
     * Get focus points of the map (aka points of interest).
     */
    private fun getFocusPoints(entities: MapLayer): List<Vector2> {
        return entities.objects.filter {
            it.getBoolean("focusPoint") && it is EllipseMapObject
        }.map {
            with((it as EllipseMapObject).ellipse) {
                Vector2(x.meters, y.meters)
            }
        }
    }

    /**
     * Get spawn point from entities layer.
     */
    private fun getSpawn(entities: MapLayer): Vector2? {
        return entities.objects.find {
            it is EllipseMapObject && SPAWN_NAME == it.name
        }?.let {
            val ellipse = (it as EllipseMapObject).ellipse
            Vector2(ellipse.x.meters, ellipse.y.meters)
        }
    }

    /**
     * Get tiled map by name.
     */
    private fun getTiledMap(name: String, internal: Boolean): TiledMap {
        val actualName = "$LEVEL_DIRECTORY$name/$name.$LEVEL_FILE_TYPE"
        if (internal) {
            return internalTmxMapLoader.load(actualName)
        }
        return tmxMapLoader.load("${Configurations.ASSETS_FOLDER}$actualName")
    }

    /**
     * Get string value from map layer properties.
     */
    private fun <T : MapLayer> T.getString(name: String): String? {
        return this.properties.get(name)?.let {
            it as String
        }
    }

    /**
     * Get float value from map layer properties.
     */
    private fun <T : MapLayer> T.getFloat(name: String, default: Float = 0f): Float {
        return this.properties.get(name)?.let {
            it as? Float ?: default
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

    /**
     * Get boolean value from map object properties.
     */
    private fun <T : MapObject> T.getBoolean(name: String, default: Boolean = false): Boolean {
        return this.properties.get(name)?.let {
            it is Boolean && it
        } ?: default
    }

    /**
     * Load all level metadata (files are loaded NOT from classpath).
     */
    private fun loadMeta(): List<JellyMapMetadata> {
        return Files
                .walk(Paths.get("${Configurations.ASSETS_FOLDER}$LEVEL_DIRECTORY"))
                .skip(1)
                .map(Path::toFile)
                .filter(File::isDirectory)
                .map {
                    JellyMapMetadata(it.name)
                }
                .collect(Collectors.toList())
    }
}