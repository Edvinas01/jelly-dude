package com.edd.jelly.systems

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.edd.jelly.components.Physics
import com.edd.jelly.components.Renderable
import com.edd.jelly.components.Transform
import com.edd.jelly.components.transform
import com.edd.jelly.util.resources.ResourceManager
import com.edd.jelly.util.resources.get
import com.google.inject.Inject
import org.jbox2d.collision.shapes.CircleShape
import org.jbox2d.collision.shapes.PolygonShape
import org.jbox2d.common.Color3f
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.BodyDef
import org.jbox2d.dynamics.BodyType
import org.jbox2d.dynamics.FixtureDef
import org.jbox2d.dynamics.World
import org.jbox2d.dynamics.joints.ConstantVolumeJointDef
import org.jbox2d.dynamics.joints.JointDef
import org.jbox2d.particle.ParticleColor
import org.jbox2d.particle.ParticleGroupDef
import org.jbox2d.particle.ParticleType

class TestSystem @Inject constructor(
        inputMultiplexer: InputMultiplexer,
        private val resources: ResourceManager,
        private val camera: Camera,
        private val world: World
) : EntitySystem() {

    /**
     * Test system mode.
     */
    private enum class Mode {
        BOX,
        CIRCLE,
        PARTICLE_BOX
    }

    private var mode = Mode.BOX
    private val font: BitmapFont

    init {
        inputMultiplexer.addProcessor(TestInputAdapter())
        font = resources.getFont()
    }

    override fun addedToEngine(engine: Engine) {
        super.addedToEngine(engine)

        createPlatforms()
        spawnJelly()
        spawnJelly()


    }

    private inner class TestInputAdapter : InputAdapter() {
        override fun keyUp(keycode: Int): Boolean {

            // Handle mode changing.
            when (keycode) {
                Input.Keys.ESCAPE -> Gdx.app.exit()

                Input.Keys.NUM_1 -> mode = Mode.BOX
                Input.Keys.NUM_2 -> mode = Mode.CIRCLE
                Input.Keys.NUM_3 -> mode = Mode.PARTICLE_BOX

                else -> {
                    return false
                }
            }

            println("Mode: $mode")
            return true
        }

        override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
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

            return true
        }

        /**
         * Spawn a random sized box.
         */
        fun spawnBox(pos: Vector2) {
            engine.addEntity(Entity().apply {
                resources.mainAtlas["the_borker"]?.let {
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
                }, 1f)
                body.setTransform(transform.position.let { v ->
                    Vec2(v.x, v.y)
                }, 0f)

                add(Physics(body))
            })
        }

        /**
         * Spawn a random sized circle.
         */
        fun spawnCircle(pos: Vector2) {
            engine.addEntity(Entity().apply {
                resources.mainAtlas["the_round_borker"]?.let {
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

                add(Physics(body))
            })
        }

        /**
         * Spawn a random sized particle box.
         */
        fun spawnParticleBox(pos: Vector2) {

            engine.addEntity(Entity().apply {
                add(Transform(
                        Vector2(pos.x, pos.y),
                        Vector2(0.1f + MathUtils.random(1f), 0.1f + MathUtils.random(1f))
                ))

//                world.createParticle() // todo for some other time
                world.createParticleGroup(ParticleGroupDef().apply {
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
                        setAsBox(transform.width, transform.height)
                    }
                    position.apply {
                        x = transform.position.x
                        y = transform.position.y
                    }
                    color = ParticleColor(Color3f(MathUtils.random(), MathUtils.random(), MathUtils.random()))
                })
            })
        }
    }

    fun spawnJelly() {
        val volumeJoint = ConstantVolumeJointDef()

        val posX = 10.0f
        val posY = 10.0f

        val width = 5.0f
        val height = 5.0f

        val bodyCount = 3
        val bodyRadius = 0.5f
        for (i in 0..bodyCount - 1) {
            val angle = org.jbox2d.common.MathUtils.map(i.toFloat(), 0f, bodyCount.toFloat(), 0f, 2 * 3.1415f)
            val bodyDef = BodyDef()
            // bd.isBullet = true;
            bodyDef.fixedRotation = true

            val x = posX + width * Math.sin(angle.toDouble()).toFloat()
            val y = posY + height * Math.cos(angle.toDouble()).toFloat()
            bodyDef.position.set(Vec2(x, y))
            bodyDef.type = BodyType.DYNAMIC
            val body = world.createBody(bodyDef)
            body.userData = "J"

            val fixtureDef = FixtureDef()
            val circle = CircleShape()
            circle.m_radius = bodyRadius
            fixtureDef.shape = circle
            fixtureDef.density = 1.0f
            body.createFixture(fixtureDef)
            volumeJoint.addBody(body)
        }

        volumeJoint.frequencyHz = 10.0f
        volumeJoint.dampingRatio = 1.0f
        volumeJoint.collideConnected = false
        val joint = world.createJoint(volumeJoint)

        println()
    }

    private fun createPlatforms() {
        val atlas = resources.mainAtlas

        engine.addEntity(Entity().apply {
            add(Renderable(atlas.findRegion("dev_grid")))
            add(Transform(
                    Vector2(5f, 5f),
                    Vector2(2.5f, 2.5f)
            ))

            val body = world.createBody(BodyDef().apply {
                type = BodyType.DYNAMIC
            })

            body.createFixture(PolygonShape().apply {
                setAsBox(transform.width / 2, transform.height / 2)
            }, 1f)
            body.setTransform(transform.position.let { v ->
                Vec2(v.x, v.y)
            }, 0f)

            add(Physics(body))
        })

        fun staticPlatform(x: Float, y: Float, width: Float, height: Float) {
            engine.addEntity(Entity().apply {
                add(Renderable(atlas.findRegion("dev_grid_tiny")))
                add(Transform(
                        Vector2(x, y),
                        Vector2(width, height)
                ))

                val body = world.createBody(BodyDef().apply {
                    type = BodyType.STATIC
                })

                body.createFixture(PolygonShape().apply {
                    setAsBox(transform.width / 2, transform.height / 2)
                }, 1f)
                body.setTransform(transform.position.let { v ->
                    Vec2(v.x, v.y)
                }, 0f)

                add(Physics(body))
            })
        }

        staticPlatform(0f, 0f, 50f, 1f)
        staticPlatform(25f, 5f, 1f, 10f)
        staticPlatform(-25f, 5f, 1f, 10f)
    }
}