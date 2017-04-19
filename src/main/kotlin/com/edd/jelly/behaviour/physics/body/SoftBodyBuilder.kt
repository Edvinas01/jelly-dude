package com.edd.jelly.behaviour.physics.body

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.maps.MapObject
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

    fun create(obj: MapObject): Entity {
        if (obj is RectangleMapObject) {
            return rectangle(obj)
        }
        return Entity()
    }

    private companion object {

        // Default values for joined mesh objects.
        val DENSITY = 4
        val DAMPING = 1f
        val STIFFNESS = 10f
        val GLUED = false
    }

    fun rectangle(rect: RectangleMapObject): Entity {
        val rectangle = rect.rectangle

        val width = rectangle.width.meters
        val height = rectangle.height.meters

        if (width <= 0 || height <= 0) {
            throw GameException("Rectangle width and height must be greater than 0")
        }

        val x = rectangle.x.meters
        val y = rectangle.y.meters

        // Density of objects that make up the soft object.
        val density = rect.int("density", DENSITY)

        val cols = MathUtils.ceil(width * density) + 1
        val rows = MathUtils.ceil(height * density) + 1

        val circleFixture = FixtureDef().apply {
            this.density = 0.1f

            restitution = 0.05f
            friction = 1f
            shape = CircleShape().apply {
                radius = 0.1f
            }
        }

        val circleDef = BodyDef().apply {
            fixedRotation = true
            type = BodyType.DYNAMIC
        }

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

        val jointDef = DistanceJointDef().apply {
            collideConnected = false
            dampingRatio = rect.float("damping", DAMPING)
            frequencyHz = rect.float("stiffness", STIFFNESS)
        }

        val glued = rect.boolean("glued", GLUED)

        // Connect the bodies.
        bodies.forEachIndexed { i, body ->

            // If rectangle is glued to the world, look for adjacent static bodies and create a joint.
            if (glued) {
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
}