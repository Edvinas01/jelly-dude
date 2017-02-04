package com.edd.jelly.behaviour.player

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.graphics.g2d.PolygonRegion
import com.badlogic.gdx.math.EarClippingTriangulator
import com.badlogic.gdx.math.Vector2
import com.edd.jelly.behaviour.components.*
import com.edd.jelly.behaviour.rendering.PolygonRenderable
import com.edd.jelly.exception.GameException
import com.edd.jelly.util.pixels
import com.edd.jelly.util.resources.ResourceManager
import com.edd.jelly.util.resources.get
import com.google.inject.Inject
import org.jbox2d.collision.shapes.CircleShape
import org.jbox2d.common.MathUtils
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.*
import org.jbox2d.dynamics.joints.ConstantVolumeJoint
import org.jbox2d.dynamics.joints.ConstantVolumeJointDef

class PlayerSystem @Inject constructor(
        private val earClippingTriangulator: EarClippingTriangulator,
        private val inputMultiplexer: InputMultiplexer,
        private val resourceManager: ResourceManager,
        private val world: World
) : IteratingSystem(Family.all(
        Transform::class.java,
        Player::class.java
).get()) {

    companion object {
        val PLAYER_TEXTURE_NAME = "the_borker"

        val PLAYER_VERTEX_COUNT = 40
        val PLAYER_VERTEX_SIZE = 0.02f

        val PLAYER_WIDTH = 1.1f
        val PLAYER_HEIGHT = 1.2f
        val PLAYER_MOVE_FORCE = 0.05f

        val PLAYER_DAMPING = 2f
        val PLAYER_DENSITY = 1f
        val PLAYER_HZ = 20f

        val PLAYER_MAX_VELOCITY = 7f
    }

    override fun addedToEngine(engine: Engine?) {
        super.addedToEngine(engine)

        // Spawn player and register its input adapter.
        inputMultiplexer.addProcessor(PlayerInputAdapter(spawnPlayer(0f, 2f).player))
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val player = entity.player

        with(player) {
            val bodies = player.joint.bodies

            for (body in bodies) {
                val velocity = body.linearVelocity

                // Up and down movement.
                if (movingUp && velocity.y < PLAYER_MAX_VELOCITY) {
                    body.applyForceToCenter(Vec2(0f, PLAYER_MOVE_FORCE * 4))
                }
                if (movingDown && velocity.y > -PLAYER_MAX_VELOCITY) {
                    body.applyForceToCenter(Vec2(0f, -PLAYER_MOVE_FORCE))
                }

                // Left and right movement.
                if (movingLeft && velocity.x > -PLAYER_MAX_VELOCITY) {
                    body.applyForceToCenter(Vec2(-PLAYER_MOVE_FORCE, 0f))
                }
                if (movingRight && velocity.x < PLAYER_MAX_VELOCITY) {
                    body.applyForceToCenter(Vec2(PLAYER_MOVE_FORCE, 0f))
                }
            }
        }
    }

    /**
     * Create polygon region from provided transform position, size and body vertices.
     */
    private fun createPlayerTexture(transform: Transform, bodies: Array<Body>): PolygonRegion {
        val playerTexture = resourceManager.mainAtlas[PLAYER_TEXTURE_NAME]!!

        val xRatio = playerTexture.regionWidth / transform.width.pixels
        val yRatio = playerTexture.regionHeight / transform.height.pixels

        val halfWidth = playerTexture.regionWidth / 2
        val halfHeight = playerTexture.regionHeight / 2

        val worldPoint = Vec2(transform.position.x, transform.position.y)
        val vertices = FloatArray(bodies.size * 2)

        bodies.forEachIndexed { i, body ->

            // x coordinates.
            vertices[i * 2] =
                    -body.getLocalPoint(worldPoint).x.pixels * xRatio + halfWidth

            // y coordinates.
            vertices[i * 2 + 1] =
                    -body.getLocalPoint(worldPoint).y.pixels * yRatio + halfHeight
        }
        return PolygonRegion(
                playerTexture,
                vertices,
                earClippingTriangulator.computeTriangles(vertices).toArray())
    }

    /**
     * Spawn player at a given location.
     */
    private fun spawnPlayer(x: Float,
                            y: Float,
                            width: Float = PLAYER_WIDTH,
                            height: Float = PLAYER_HEIGHT): Entity {

        val players = engine.getEntitiesFor(Family
                .all(Player::class.java)
                .get())

        // For now only one player is allowed.
        if (players.size() > 0) {
            throw GameException("Only one player can exist")
        }

        val entity = Entity().apply {
            val joint = world.createJoint(ConstantVolumeJointDef().apply {
                collideConnected = false
                dampingRatio = PLAYER_DAMPING
                frequencyHz = PLAYER_HZ

                // We're going round in a circle, radius will be double, so have to divide by two.
                val halfWidth = width / 2
                val halfHeight = height / 2

                for (vertex in 0..PLAYER_VERTEX_COUNT - 1) {

                    // Should add bodies as entities to the engine?
                    addBody(world.createBody(BodyDef().apply {
                        val angle = MathUtils.map(
                                vertex.toFloat(),
                                0f,
                                PLAYER_VERTEX_COUNT.toFloat(),
                                0f,
                                2 * MathUtils.PI)

                        fixedRotation = true
                        position = Vec2(
                                x + halfWidth * MathUtils.sin(angle),
                                y + halfHeight * MathUtils.cos(angle))

                        type = BodyType.DYNAMIC

                    }).apply {
                        createFixture(FixtureDef().apply {
                            density = PLAYER_DENSITY
                            shape = CircleShape().apply {
                                radius = PLAYER_VERTEX_SIZE
                            }
                        })
                    })
                }
            }) as ConstantVolumeJoint

            val transform = Transform(
                    Vector2(x, y),
                    Vector2(width, height))

            // Register player entity components.
            add(PolygonRenderable(createPlayerTexture(transform, joint.bodies)))
            add(Player(joint))
            add(transform)
        }
        engine.addEntity(entity)
        return entity
    }
}