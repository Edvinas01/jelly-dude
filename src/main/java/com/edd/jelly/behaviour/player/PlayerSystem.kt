package com.edd.jelly.behaviour.player

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.g2d.PolygonRegion
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.EarClippingTriangulator
import com.badlogic.gdx.math.Vector2
import com.edd.jelly.components.*
import com.edd.jelly.exception.GameException
import com.edd.jelly.util.meters
import com.edd.jelly.util.pixels
import com.edd.jelly.util.resources.ResourceManager
import com.edd.jelly.util.resources.get
import com.google.inject.Inject
import org.jbox2d.collision.shapes.CircleShape
import org.jbox2d.common.MathUtils
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.BodyDef
import org.jbox2d.dynamics.BodyType
import org.jbox2d.dynamics.FixtureDef
import org.jbox2d.dynamics.World
import org.jbox2d.dynamics.joints.ConstantVolumeJoint
import org.jbox2d.dynamics.joints.ConstantVolumeJointDef
import kotlin.system.measureNanoTime
import kotlin.system.measureTimeMillis

class PlayerSystem @Inject constructor(
        private val inputMultiplexer: InputMultiplexer,
        private val resourceManager: ResourceManager,
        private val polygonBatch: PolygonSpriteBatch,
        private val camera: Camera,
        private val world: World
) : IteratingSystem(Family.all(
        Transform::class.java,
        PlayerComponent::class.java
).get()) {

    companion object {
        val PLAYER_VERTEX_COUNT = 40
        val PLAYER_VERTEX_SIZE = 0.02f

        val PLAYER_WIDTH = 0.55f
        val PLAYER_HEIGHT = 0.6f
        val PLAYER_MOVE_FORCE = 0.05f

        val PLAYER_DAMPING = 2f
        val PLAYER_DENSITY = 1f
        val PLAYER_HZ = 20f

        val PLAYER_MAX_VELOCITY = 7f
    }

    val triangulator = EarClippingTriangulator()

    lateinit var playerPolygonRegion: PolygonRegion
    lateinit var playerTexture: TextureRegion

    override fun addedToEngine(engine: Engine?) {
        super.addedToEngine(engine)

        playerTexture = resourceManager.mainAtlas["dev_grid"]!!

        // Spawn player and register its input adapter.
        val player = spawnPlayer(0f, 2f)
        val transform = player.transform
        val spawned = player.player

        inputMultiplexer.addProcessor(PlayerInputAdapter(spawned))

        val vertices = FloatArray(spawned.joint.bodies.size * 2)
        spawned.joint.bodies.forEachIndexed { i, body ->
            val vec = Vec2(transform.position.x, transform.position.y)
            vertices[i * 2]     = -body.getLocalPoint(vec).x.pixels * playerTexture.regionWidth.meters  + playerTexture.regionWidth  / 2
            vertices[i * 2 + 1] = -body.getLocalPoint(vec).y.pixels * playerTexture.regionHeight.meters + playerTexture.regionHeight / 2
        }

        val triangles = triangulator.computeTriangles(vertices)
        playerPolygonRegion = PolygonRegion(playerTexture, vertices, triangles.toArray())
    }

    override fun update(deltaTime: Float) {
        polygonBatch.projectionMatrix = camera.combined

        polygonBatch.begin()
        super.update(deltaTime)
        polygonBatch.end()
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val transform = entity.transform
        val player = entity.player

        with(player) {
            val averagePosition = Vector2()
            val bodies = player.joint.bodies

            for (body in bodies) {
                averagePosition.x += body.position.x
                averagePosition.y += body.position.y

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

            // Calculate average position of all bodies, center of the blob.
            transform.position.x = averagePosition.x / bodies.size
            transform.position.y = averagePosition.y / bodies.size
        }

        polygonBatch.draw(playerPolygonRegion,
                transform.x,
                transform.y,
                playerTexture.regionWidth.meters,
                playerTexture.regionHeight.meters)

        player.joint.bodies.forEachIndexed { i, body ->
            val vec = Vec2(transform.position.x, transform.position.y)

            playerPolygonRegion.vertices[i * 2] = -body.getLocalPoint(vec).x.pixels
            playerPolygonRegion.vertices[i * 2 + 1] = -body.getLocalPoint(vec).y.pixels
        }
        triangulator.computeTriangles(playerPolygonRegion.vertices).toArray().forEachIndexed { i, triangle ->
            playerPolygonRegion.triangles[i] = triangle
        }
    }

    /**
     * Spawn player at a given location.
     */
    private fun spawnPlayer(x: Float, y: Float): Entity {
        val players = engine.getEntitiesFor(Family
                .all(PlayerComponent::class.java)
                .get())

        // For now only one player is allowed.
        if (players.size() > 0) {
            throw GameException("Only one player can exist")
        }

        val entity = Entity().apply {
            val transform = Transform(
                    Vector2(x, y),
                    Vector2(PLAYER_WIDTH, PLAYER_HEIGHT))

            val joint = world.createJoint(ConstantVolumeJointDef().apply {
                collideConnected = false
                dampingRatio = PLAYER_DAMPING
                frequencyHz = PLAYER_HZ

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
                                x + transform.width * MathUtils.sin(angle),
                                y + transform.height * MathUtils.cos(angle))

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

            // Register player entity components.
            add(Renderable(playerTexture))
            add(PlayerComponent(joint))
            add(transform)
        }
        engine.addEntity(entity)
        return entity
    }
}