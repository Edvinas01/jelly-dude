package com.edd.jelly.behaviour.level

import com.badlogic.gdx.maps.MapObject
import com.badlogic.gdx.maps.MapObjects
import com.badlogic.gdx.maps.objects.EllipseMapObject
import com.badlogic.gdx.maps.objects.PolygonMapObject
import com.badlogic.gdx.maps.objects.PolylineMapObject
import com.badlogic.gdx.maps.objects.RectangleMapObject
import com.badlogic.gdx.maps.tiled.TiledMap
import com.edd.jelly.exception.GameException
import com.edd.jelly.util.Units
import com.edd.jelly.util.meters
import org.jbox2d.collision.shapes.ChainShape
import org.jbox2d.collision.shapes.CircleShape
import org.jbox2d.collision.shapes.PolygonShape
import org.jbox2d.collision.shapes.Shape
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
    private val bodyType = BodyType.STATIC
    private val density = 1f

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
            world.createBody(BodyDef().apply {
                type = bodyType
            }).apply {
                create(it).apply {
                    setTransform(first, 0f)
                    createFixture(second, density)
                }
            }
        }
    }

    /**
     * Create a pair of shape location and the shape by using map object data.
     */
    private fun create(mapObject: MapObject): Pair<Vec2, Shape> = when (mapObject) {
        is RectangleMapObject -> rectangle(mapObject)
        is EllipseMapObject -> ellipse(mapObject)
        is PolygonMapObject -> polygon(mapObject)
        is PolylineMapObject -> chain(mapObject)
        else -> {
            throw GameException("Unsupported map object type: ${mapObject.javaClass.simpleName}")
        }
    }

    /**
     * Create rectangle from map object.
     */
    private fun rectangle(mapObject: RectangleMapObject): Pair<Vec2, PolygonShape> = with(mapObject.rectangle) {
        Pair(Vec2(x + width / 2, y + height / 2).mul(Units.MPP), PolygonShape().apply {
            setAsBox(width.meters / 2, height.meters / 2)
        })
    }

    /**
     * Create ellipse from map object.
     */
    private fun ellipse(mapObject: EllipseMapObject): Pair<Vec2, CircleShape> = with(mapObject.ellipse) {
        Pair(Vec2(x + width / 2, y + height / 2).mul(Units.MPP), CircleShape().apply {
            radius = height.meters / 2
        })
    }

    /**
     * Create polygon from map object.
     */
    private fun polygon(mapObject: PolygonMapObject): Pair<Vec2, PolygonShape> = with(mapObject.polygon) {
        Pair(Vec2(x, y).mul(Units.MPP), PolygonShape().apply {
            getVectorArray(this@with.vertices).let {
                set(it, it.size)
            }
        })
    }

    /**
     * Create chain from map object.
     */
    private fun chain(mapObject: PolylineMapObject): Pair<Vec2, ChainShape> = with(mapObject.polyline) {
        Pair(Vec2(x, y).mul(Units.MPP), ChainShape().apply {
            getVectorArray(this@with.vertices).let {
                createChain(it, it.size)
            }
        })
    }

    /**
     * Get vector array from provided float array, where x and y values are laid out next to each other.
     */
    private fun getVectorArray(vertices: FloatArray): Array<Vec2> {
        val vectors = mutableListOf<Vec2>()
        for (i in 0..vertices.size / 2 - 1) {
            vectors.add(Vec2(vertices[i * 2].meters, vertices[i * 2 + 1].meters))
        }
        return vectors.toTypedArray()
    }
}