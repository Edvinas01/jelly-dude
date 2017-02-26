package com.edd.jelly.behaviour.level

import com.badlogic.gdx.maps.MapObject
import com.badlogic.gdx.maps.MapObjects
import com.badlogic.gdx.maps.objects.RectangleMapObject
import com.badlogic.gdx.maps.tiled.TiledMap
import com.edd.jelly.exception.GameException
import com.edd.jelly.util.meters
import org.jbox2d.collision.shapes.PolygonShape
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.Body
import org.jbox2d.dynamics.BodyDef
import org.jbox2d.dynamics.BodyType
import org.jbox2d.dynamics.World

/**
 * Builds bodies for tiled map.
 */
class MapBodyBuilder private constructor(private val world: World) {
    private val objects = mutableListOf<MapObject>()

    companion object {
        fun usingWorld(world: World): MapBodyBuilder {
            return MapBodyBuilder(world)
        }
    }

    /**
     * Set tiled map objects which are to be used when building bodies by using a specific layer.
     */
    fun tiledMapLayer(tiledMap: TiledMap, layer: String): MapBodyBuilder {
        return objects(tiledMap.layers[layer]?.objects ?:
                throw GameException("$layer layer does not exist on tiled map"))
    }

    /**
     * Set tiled map objects which are to be used when building bodies.
     */
    fun objects(objects: MapObjects): MapBodyBuilder {
        this.objects.clear()
        objects.forEach {
            this.objects.add(it)
        }
        return this
    }

    /**
     * Build bodies using the provided data for the builder.
     */
    fun buildBodies(): List<Body> {
        return objects.map {
            when (it) {
                is RectangleMapObject -> {
                    create(it)
                }
                else -> {
                    throw GameException("Unsupported supported type: ${it.javaClass.name}")
                }
            }
        }.map {
            val body = world.createBody(BodyDef().apply {
                type = BodyType.STATIC
            })
            body.createFixture(it, 1f)
            body
        }
    }

    /**
     * Create polygon shape from rectangle.
     */
    private fun create(rectangleObject: RectangleMapObject): PolygonShape {
        val rectangle = rectangleObject.rectangle
        val polygon = PolygonShape()
        val size = Vec2(
                (rectangle.x + rectangle.width * 0.5f).meters,
                (rectangle.y + rectangle.height * 0.5f).meters
        )

        polygon.setAsBox(
                rectangle.width.meters * 0.5f,
                rectangle.height.meters * 0.5f,
                size,
                0.0f)

        return polygon
    }
}