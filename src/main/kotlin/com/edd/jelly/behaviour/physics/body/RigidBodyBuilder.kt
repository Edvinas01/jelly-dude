package com.edd.jelly.behaviour.physics.body

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.maps.MapObject
import com.badlogic.gdx.maps.objects.RectangleMapObject
import com.edd.jelly.behaviour.position.Transform
import com.edd.jelly.behaviour.rendering.Renderable
import com.edd.jelly.core.resources.ResourceManager
import com.edd.jelly.core.resources.get
import com.edd.jelly.core.tiled.float
import com.edd.jelly.core.tiled.string
import com.edd.jelly.util.meters
import com.edd.jelly.util.radians
import com.edd.jelly.util.toVector2
import com.google.inject.Inject
import com.google.inject.Singleton
import org.jbox2d.collision.shapes.PolygonShape
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.BodyDef
import org.jbox2d.dynamics.BodyType
import org.jbox2d.dynamics.FixtureDef
import org.jbox2d.dynamics.World

/**
 * Builds simple rigid bodies.
 */
@Singleton
class RigidBodyBuilder @Inject constructor(
        private val resources: ResourceManager,
        private val world: World
) : BodyBuilder {

    // Default object values.
    private companion object {
        const val FRICTION = 0.8f
        const val DENSITY = 0.1f
    }

    override fun create(mapObject: MapObject) = when (mapObject) {
        is RectangleMapObject -> createRectangle(mapObject)
        else -> null
    }

    /**
     * Create rectangle shape from map object.
     */
    private fun createRectangle(obj: RectangleMapObject): Entity {
        val rect = obj.rectangle

        val x = rect.x.meters
        val y = rect.y.meters

        val width = rect.width.meters
        val height = rect.height.meters

        val halfWidth = width / 2
        val halfHeight = height / 2

        val pos = Vec2(x + halfWidth, y + halfHeight)
        val size = Vec2(width, height)

        val shape = PolygonShape().apply {
            setAsBox(halfWidth, halfHeight)
        }

        val rotation = obj.rotation
        val body = world.createBody(BodyDef().apply {
            type = obj.bodyType
        }).apply {
            val fixture = FixtureDef().apply {
                friction = obj.friction
                density = obj.density
                this.shape = shape
            }

            createFixture(fixture)
            setTransform(pos, 0f)

            val angle = -rotation.radians

            // Rectangles in Tiled are anchored on top left?
            val localPosition = Vec2(-width / 2, height / 2)
            val positionBefore = getWorldPoint(localPosition)

            setTransform(position, angle)

            val positionAfter = getWorldPoint(localPosition)
            setTransform(position
                    .add(positionBefore)
                    .sub(positionAfter),
                    angle
            )
        }

        return Entity().apply {
            add(RigidBody(body))
            add(Renderable(obj.texture))
            add(Transform(
                    position = pos.toVector2(),
                    size = size.toVector2(),
                    rotation = rotation
            ))
        }
    }

    /**
     * Get object rotation (degrees).
     */
    private val MapObject.rotation: Float
        get() = float("rotation")

    /**
     * Get texture region from map object.
     */
    private val MapObject.texture: TextureRegion
        get(): TextureRegion {
            val name = string("texture") ?: ""
            return resources.atlas[name] ?: resources.getRegion(name)
        }

    /**
     * Get map object body type.
     */
    private val MapObject.bodyType: BodyType
        get() : BodyType = when (string("bodyType")) {
            "kinematic" -> BodyType.KINEMATIC
            "dynamic" -> BodyType.DYNAMIC
            "static" -> BodyType.STATIC
            else -> BodyType.DYNAMIC
        }

    /**
     * Get map object friction.
     */
    private val MapObject.friction: Float
        get() = float("friction", FRICTION)

    /**
     * Get map object density.
     */
    private val MapObject.density: Float
        get() = float("density", DENSITY)
}