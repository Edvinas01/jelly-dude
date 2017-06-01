package com.edd.jelly.behaviour.test

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.edd.jelly.behaviour.position.Transform
import com.edd.jelly.behaviour.position.transform
import com.edd.jelly.behaviour.physics.Particles
import com.edd.jelly.behaviour.physics.body.RigidBody
import com.edd.jelly.behaviour.rendering.Renderable
import com.edd.jelly.behaviour.common.event.ConfigChangedEvent
import com.edd.jelly.behaviour.common.event.PlaySoundEvent
import com.edd.jelly.behaviour.physics.body.SoftBody
import com.edd.jelly.core.configuration.Configurations
import com.edd.jelly.core.events.Messaging
import com.edd.jelly.core.resources.ResourceManager
import com.edd.jelly.core.resources.get
import com.edd.jelly.util.pixels
import com.edd.jelly.util.toVec2
import com.google.inject.Inject
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.jbox2d.callbacks.QueryCallback
import org.jbox2d.collision.AABB
import org.jbox2d.collision.shapes.CircleShape
import org.jbox2d.collision.shapes.PolygonShape
import org.jbox2d.common.Color3f
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.*
import org.jbox2d.dynamics.joints.*
import org.jbox2d.particle.ParticleColor
import org.jbox2d.particle.ParticleGroup
import org.jbox2d.particle.ParticleGroupDef
import org.jbox2d.particle.ParticleType
import java.lang.NullPointerException

class TestSystem @Inject constructor(
        private val configurations: Configurations,
        private val resources: ResourceManager,
        private val camera: OrthographicCamera,
        private val messaging: Messaging,
        private val world: World,
        inputMultiplexer: InputMultiplexer
) : EntitySystem() {

    private companion object {
        const val BOING_SOUND_NAME = "boing"
        const val BOING_LOW_PITCH = 0.8f
        const val BOING_VELOCITY = 3f

        val LOG: Logger = LogManager.getLogger(TestSystem::class.java)
    }

    /**
     * Test system mode.
     */
    private enum class Mode {
        BOX,
        CIRCLE,
        PARTICLE_BOX
    }

    private val adapter = TestInputAdapter()
    private val mouse = Vector3()
    private var mode = Mode.BOX

    // Stats.
    private var totalFrames = 0L
    private var totalTicks = 0L

    private var highestFrames = Int.MIN_VALUE
    private var lowestFrames = Int.MAX_VALUE

    // Mouse dragging.
    private val jointDef = MouseJointDef().apply {
        collideConnected = true
        maxForce = 100f
    }

    private var joint: MouseJoint? = null
    private var tmpGdx = Vector3()
    private var tmpBox = Vec2()

    private val queryCallback = QueryCallback {
        jointDef.bodyA = it.body
        jointDef.bodyB = it.body
        jointDef.target.set(tmpGdx.x, tmpGdx.y)
        joint = world.createJoint(jointDef) as MouseJoint

        false
    }

    init {
        inputMultiplexer.addProcessor(adapter)
        enable(configurations.config.game.debug)
    }

    override fun addedToEngine(engine: Engine) {
        super.addedToEngine(engine)

        messaging.listen<ConfigChangedEvent> { (c) ->
            enable(c.game.debug)
        }
    }

    override fun removedFromEngine(engine: Engine) {
        if (totalTicks > 0) {
            LOG.debug("Average fps: {}, highest fps: {}, lowest fps: {}",
                    totalFrames / totalTicks, highestFrames, lowestFrames)

        } else {
            LOG.warn("Got 0 ticks, cannot calculate fps stats")
        }
    }

    private fun enable(enable: Boolean) {
        setProcessing(enable)
        adapter.enabled = enable
    }

    override fun update(deltaTime: Float) {
        trackStats()

        camera.unproject(mouse.set(Gdx.input.x.toFloat(), Gdx.input.y.toFloat(), 0f))

        Gdx.graphics.setTitle(String.format("meters (%.3f, %.3f) | pixels (%.0f, %.0f) | fps %d",
                mouse.x, mouse.y, mouse.x.pixels, mouse.y.pixels, Gdx.graphics.framesPerSecond))
    }

    private fun trackStats() {
        val frames = Gdx.graphics.framesPerSecond

        highestFrames = Math.max(highestFrames, frames)
        lowestFrames = Math.min(lowestFrames, frames)

        totalFrames += frames
        totalTicks++
    }

    private inner class TestInputAdapter : InputAdapter() {

        var enabled = true

        override fun keyUp(keycode: Int): Boolean {

            // Handle debug toggling.
            if (Input.Keys.GRAVE == keycode) {
                configurations.config.game.debug = !configurations.config.game.debug
                configurations.save()

                return true
            }

            if (!enabled) {
                return false
            }

            when (keycode) {

            // Handle mode changing.
                Input.Keys.NUM_1 -> mode = Mode.BOX
                Input.Keys.NUM_2 -> mode = Mode.CIRCLE
                Input.Keys.NUM_3 -> mode = Mode.PARTICLE_BOX

                else -> {
                    return false
                }
            }

            LOG.debug("Changed mode to: {}", mode)
            return true
        }

        override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
            if (!enabled) {
                return false
            }

            if (Input.Buttons.RIGHT == button) {
                camera.unproject(tmpGdx.set(screenX.toFloat(), screenY.toFloat(), 0f))

                world.queryAABB(queryCallback, AABB(tmpGdx.toVec2(), tmpGdx.toVec2()).apply {
                    val offset = 0.05f

                    lowerBound.addLocal(-offset, -offset)
                    upperBound.addLocal(offset, offset)
                })
                return true
            }
            return false
        }

        override fun touchDragged(screenX: Int, screenY: Int, pointer: Int) =
                if (joint != null) {
                    camera.unproject(tmpGdx.set(screenX.toFloat(), screenY.toFloat(), 0f))
                    joint!!.target = tmpBox.set(tmpGdx.x, tmpGdx.y)
                    true
                } else false

        override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
            if (Input.Buttons.RIGHT == button && joint != null) {

                // Play a sound on joint release.
                val data = joint?.bodyB?.userData
                if (data is SoftBody) {

                    var vel = 0f
                    data.bodies.forEach {
                        vel += it.linearVelocity.length()
                    }
                    vel /= data.bodies.size

                    if (vel > BOING_VELOCITY) {
                        messaging.send(PlaySoundEvent(name = BOING_SOUND_NAME, lowPitch = BOING_LOW_PITCH))
                    }
                }

                world.destroyJoint(joint)
                joint = null
                return true
            }

            if (!enabled) {
                return false
            }

            if (Input.Buttons.LEFT != button) {
                return false
            }

            val pos = camera.unproject(Vector3(screenX.toFloat(), screenY.toFloat(), 0f)).let { v ->
                Vector2(v.x, v.y)
            }

            when (mode) {
                Mode.BOX -> spawnBox(pos)
                Mode.CIRCLE -> spawnCircle(pos)
                Mode.PARTICLE_BOX -> spawnParticleBox(pos)
            }

            LOG.debug("Clicked at: {}", pos)
            return true
        }

        /**
         * Spawn a random sized box.
         */
        private fun spawnBox(pos: Vector2) {
            engine.addEntity(Entity().apply {
                resources.atlas["crate"]?.let {
                    add(Renderable(it))
                }

                add(Transform(
                        Vector2(pos.x, pos.y),
                        Vector2(0.5f + MathUtils.random(1f), 0.5f + MathUtils.random(1f))
                ))

                val body = world.createBody(BodyDef().apply {
                    type = BodyType.DYNAMIC
                })

                body.createFixture(PolygonShape().apply {
                    setAsBox(transform.width / 2, transform.height / 2)
                }, 0.1f)
                body.setTransform(transform.position.let { v ->
                    Vec2(v.x, v.y)
                }, 0f)

                add(RigidBody(body))
            })
        }

        /**
         * Spawn a random sized circle.
         */
        private fun spawnCircle(pos: Vector2) {
            engine.addEntity(Entity().apply {
                resources.atlas["round_crate"]?.let {
                    add(Renderable(it))
                }

                val radius = 0.5f + MathUtils.random(1f)
                add(Transform(
                        Vector2(pos.x, pos.y),
                        Vector2(radius, radius)
                ))

                val body = world.createBody(BodyDef().apply {
                    type = BodyType.DYNAMIC
                })

                body.createFixture(CircleShape().apply {
                    this.radius = transform.width / 2
                }, 1f)
                body.setTransform(transform.position.let { v ->
                    Vec2(v.x, v.y)
                }, 0f)

                add(RigidBody(body))
            })
        }

        /**
         * Spawn a random sized particle box.
         */
        private fun spawnParticleBox(pos: Vector2) {

            val w = 0.1f + MathUtils.random(1f)
            val h = 0.1f + MathUtils.random(1f)

            engine.addEntity(Entity().apply {
                //                add(Transform(
//                        Vector2(pos.x, pos.y),
//                        Vector2(0.1f + MathUtils.random(1f), 0.1f + MathUtils.random(1f))
//                ))

                try {
                    val group: ParticleGroup? = world.createParticleGroup(ParticleGroupDef().apply {
                        val particleTypes = listOf(
                                ParticleType.b2_waterParticle,
                                ParticleType.b2_springParticle,
                                ParticleType.b2_elasticParticle,
                                ParticleType.b2_viscousParticle,
                                ParticleType.b2_powderParticle,
                                ParticleType.b2_tensileParticle,
                                ParticleType.b2_colorMixingParticle
                        )

                        flags = particleTypes[MathUtils.random(particleTypes.size - 1)]
                        shape = PolygonShape().apply {
                            setAsBox(w, h)
                        }
                        position.apply {
                            x = pos.x
                            y = pos.y
                        }
                        color = ParticleColor(Color3f(MathUtils.random(), MathUtils.random(), MathUtils.random()))
                        color.a = 5
                    })

                    if (group != null) {
                        add(Particles(group))
                    }

                } catch (e: NullPointerException) {
                    // liquid fun seems to throw this somewhere deep in its code, this is a test system
                    // so im not gonna bother.
                    LOG.error("Could not spawn particles", e)
                }
            })
        }
    }

    private fun spawnCoolJelly(pos: Vector2,
                               width: Float = MathUtils.random(0.7f),
                               height: Float = MathUtils.random(0.5f),
                               bodyCount: Int = MathUtils.random(5, 20)): ConstantVolumeJoint {

        val transform = Transform(
                pos,
                Vector2(0.3f + width,
                        0.1f + height))

        return world.createJoint(ConstantVolumeJointDef().apply {
            val circleRadius = 0.02f

            for (i in 0..bodyCount - 1) {
                addBody(world.createBody(BodyDef().apply {
                    val angle = org.jbox2d.common.MathUtils.map(
                            i.toFloat(),
                            0f,
                            bodyCount.toFloat(),
                            0f,
                            2 * MathUtils.PI)

                    fixedRotation = true
                    type = BodyType.DYNAMIC
                    position.set(Vec2(
                            pos.x + transform.width * Math.sin(angle.toDouble()).toFloat(),
                            pos.y + transform.height * Math.cos(angle.toDouble()).toFloat()))
                }).apply {
                    createFixture(FixtureDef().apply {
                        shape = CircleShape().apply {
                            radius = circleRadius
                        }
                        density = 1f
                    })
                })
            }
            frequencyHz = 20f
            dampingRatio = 1f
            collideConnected = false
        }) as ConstantVolumeJoint
    }
}