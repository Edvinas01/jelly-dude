package com.edd.jelly.behaviour.physics.body

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.maps.MapObject
import com.badlogic.gdx.maps.objects.EllipseMapObject
import com.badlogic.gdx.maps.objects.PolygonMapObject
import com.badlogic.gdx.maps.objects.PolylineMapObject
import com.badlogic.gdx.maps.objects.RectangleMapObject
import com.edd.jelly.behaviour.physics.Physics
import com.edd.jelly.util.Units
import com.edd.jelly.util.meters
import com.google.inject.Inject
import com.google.inject.Singleton
import org.jbox2d.collision.shapes.ChainShape
import org.jbox2d.collision.shapes.CircleShape
import org.jbox2d.collision.shapes.PolygonShape
import org.jbox2d.collision.shapes.Shape
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.BodyDef
import org.jbox2d.dynamics.BodyType
import org.jbox2d.dynamics.World

/**
 * Builds bodies for tiled map.
 */
@Singleton
class MapBodyBuilder @Inject constructor(private val world: World) {

    private companion object {
        const val DENSITY = 1f
        val BODY_TYPE = BodyType.STATIC
    }

    fun create(obj: MapObject): Entity? {
        val pair = createPair(obj) ?: return null

        return Entity().apply {
            add(Physics(world.createBody(BodyDef().apply {
                type = BODY_TYPE
            }).apply {
                setTransform(pair.first, 0f)
                createFixture(pair.second, DENSITY)
            }))
        }
    }

    /**
     * Create a pair of shape location and the shape by using map object data.
     */
    private fun createPair(mapObject: MapObject): Pair<Vec2, Shape>? = when (mapObject) {
        is RectangleMapObject -> rectangle(mapObject)
        is EllipseMapObject -> ellipse(mapObject)
        is PolygonMapObject -> polygon(mapObject)
        is PolylineMapObject -> chain(mapObject)
        else -> null
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