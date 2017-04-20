package com.edd.jelly.behaviour.physics.body

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.maps.MapObject
import com.badlogic.gdx.maps.objects.EllipseMapObject
import com.badlogic.gdx.maps.objects.RectangleMapObject
import com.edd.jelly.util.meters
import com.google.inject.Inject
import com.google.inject.Singleton
import org.jbox2d.collision.shapes.CircleShape
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.*
import com.badlogic.gdx.math.MathUtils
import com.edd.jelly.behaviour.components.Transform
import com.edd.jelly.core.tiled.boolean
import com.edd.jelly.core.tiled.float
import com.edd.jelly.core.tiled.int
import com.edd.jelly.exception.GameException
import org.jbox2d.callbacks.QueryCallback
import org.jbox2d.collision.AABB
import org.jbox2d.dynamics.joints.DistanceJointDef
import org.jbox2d.dynamics.joints.RevoluteJointDef

@Singleton
class SoftBodyBuilder @Inject constructor(val world: World) {

    private companion object {
        const val RADIUS = 0.1f

        // Default values for joined mesh objects.
        const val DENSITY = 4
        const val DAMPING = 1f
        const val STIFFNESS = 10f
    }

    private val circleFixture = FixtureDef().apply {
        this.density = 0.1f

        restitution = 0.05f
        friction = 1f
        shape = CircleShape().apply {
            radius = RADIUS
        }
    }

    // This gets mutated, each time that happens, its position is set to a new one so I guess its ok.
    private val circleDef = BodyDef().apply {
        fixedRotation = true
        type = BodyType.DYNAMIC
    }

    /**
     * Create a soft map object.
     *
     * @return created soft map object entity.
     */
    fun create(obj: MapObject): Entity? {
        if (obj is RectangleMapObject) {
            return rectangle(obj)
        } else if (obj is EllipseMapObject) {
            return ellipse(obj)
        }
        return null
    }

    /**
     * Create a soft ellipse.
     */
    fun ellipse(ellipseMapObject: EllipseMapObject): Entity {
        val ellipse = ellipseMapObject.ellipse

        val x = ellipse.x.meters
        val y = ellipse.y.meters

        val width = ellipse.width.meters
        val height = ellipse.height.meters

        val halfWidth = width / 2
        val halfHeight = height / 2

        if (width <= 0 || height <= 0) {
            throw GameException("Ellipse width and height must be greater than 0")
        }

        val bodies = mutableListOf<Body>()
        val center = Vec2(x + halfWidth, y + halfHeight)

        // Count how many circles should make up the object.
        val density = ellipseMapObject.int("density", DENSITY)
        val ci = (width * density + height * density).toInt() * 2

        (0 until ci)
                .map {
                    org.jbox2d.common.MathUtils.map(
                            it.toFloat(),
                            0f,
                            ci.toFloat(),
                            0f,
                            2 * org.jbox2d.common.MathUtils.PI
                    )
                }
                .forEach {
                    bodies += world.createBody(circleDef.apply {
                        position.x = center.x + halfWidth * MathUtils.sin(it)
                        position.y = center.y + halfHeight * MathUtils.cos(it)
                    }).apply {
                        createFixture(circleFixture)
                    }
                }

        // The central circle.
        val centerBody = world.createBody(circleDef.apply {
            position.set(center)
        }).apply {
            createFixture(circleFixture)
        }

        val glued = ellipseMapObject.glued
        bodies += centerBody

        // Join the circles.
        val jointDef = getJointDef(ellipseMapObject)
        for (i in 0 until ci) {

            // The neighbor.
            val neighborIndex = (i + 1) % ci

            // Get the current body and the neighbor.
            val currentBody = bodies[i]
            val neighborBody = bodies[neighborIndex]

            // Connect the outer circles to each other.
            jointDef.initialize(
                    currentBody,
                    neighborBody,
                    currentBody.worldCenter,
                    neighborBody.worldCenter
            )

            world.createJoint(jointDef)

            // Connect the center circle with other circles.
            jointDef.initialize(currentBody, centerBody, currentBody.worldCenter, center)
            world.createJoint(jointDef)

            if (glued) {
                glue(currentBody)
            }
        }

        return Entity()
    }

    /**
     * Create a soft rectangle.
     */
    fun rectangle(rect: RectangleMapObject): Entity {
        val rectangle = rect.rectangle

        // Take joined body radius into account when creating bodies.
        val offset = RADIUS * 2

        var width = rectangle.width.meters
        var height = rectangle.height.meters

        width = if (width < offset) offset else width - offset
        height = if (height < offset) offset else height - offset

        if (width <= 0 || height <= 0) {
            throw GameException("Rectangle width and height must be greater than 0")
        }

        val x = rectangle.x.meters + RADIUS
        val y = rectangle.y.meters + RADIUS

        // Density of objects that make up the soft object.
        val density = rect.int("density", DENSITY)

        val cols = MathUtils.ceil(width * density) + 1
        val rows = MathUtils.ceil(height * density) + 1

        val stepX = width / (cols - 1)
        val stepY = height / (rows - 1)

        val bodies = mutableListOf<Body>()

        // Create a rectangle out of the bodies.
        for (row in 0 until rows) {
            for (col in 0 until cols) {
                bodies += world.createBody(circleDef.apply {
                    position = Vec2(x + col * stepX, y + row * stepY)
                }).apply {
                    createFixture(circleFixture)
                }
            }
        }

        val jointDef = getJointDef(rect)
        val glued = rect.glued

        // Connect the bodies.
        bodies.forEachIndexed { i, body ->
            if (glued) {
                glue(body)
            }

            // Join left neighbours.
            if (i % cols > 0) {
                bodies[i - 1].let {
                    jointDef.initialize(body, it, body.worldCenter, it.worldCenter)
                    world.createJoint(jointDef)
                }
            }

            // Join bottom neighbours.
            if (i - cols >= 0) {
                bodies[i - cols].let {
                    jointDef.initialize(body, it, body.worldCenter, it.worldCenter)
                    world.createJoint(jointDef)
                }
            }

            // Joint bottom left neighbours.
            if (i - cols >= 0 && i % cols > 0) {
                bodies[i - cols - 1].let {
                    jointDef.initialize(body, it, body.worldCenter, it.worldCenter)
                    world.createJoint(jointDef)
                }
            }

            // Join top left neighbours.
            if ((i + cols) % cols > 0 && i + cols < bodies.size) {
                bodies[i + cols - 1].let {
                    jointDef.initialize(body, it, body.worldCenter, it.worldCenter)
                    world.createJoint(jointDef)
                }
            }
        }

        return Entity().apply {
            add(SoftBody(bodies.toList()))
            add(Transform())
        }
    }

    /**
     * Helper function to get joint definition with adjusted parameters based on map object.
     */
    private fun getJointDef(obj: MapObject) = DistanceJointDef().apply {
        collideConnected = false
        dampingRatio = obj.float("damping", DAMPING)
        frequencyHz = obj.float("stiffness", STIFFNESS)
    }

    /**
     * Attempt to glue provided body to bodies that are nearby it.
     */
    private fun glue(body: Body) {
        world.queryAABB(QueryCallback {
            val found = it.body
            if (BodyType.STATIC == found.type) {
                world.createJoint(RevoluteJointDef().apply {
                    this.initialize(body, found, body.worldCenter)
                    collideConnected = false
                })
                return@QueryCallback false
            }
            true
        }, AABB(body.position, body.position))
    }

    /**
     * Helper extension to see if map object is glued.
     */
    private val <T : MapObject> T.glued: Boolean
        get() = this.boolean("glued")
}