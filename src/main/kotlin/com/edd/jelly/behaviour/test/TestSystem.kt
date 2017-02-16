package com.edd.jelly.behaviour.test

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.PolygonRegion
import com.badlogic.gdx.math.EarClippingTriangulator
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.edd.jelly.behaviour.components.Transform
import com.edd.jelly.behaviour.components.transform
import com.edd.jelly.behaviour.physics.Physics
import com.edd.jelly.behaviour.rendering.PolygonRenderable
import com.edd.jelly.behaviour.rendering.Renderable
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
import org.jbox2d.dynamics.joints.ConstantVolumeJoint
import org.jbox2d.dynamics.joints.ConstantVolumeJointDef
import org.jbox2d.particle.ParticleColor
import org.jbox2d.particle.ParticleGroupDef
import org.jbox2d.particle.ParticleType

class TestSystem @Inject constructor(
        inputMultiplexer: InputMultiplexer,
        private val earClippingTriangulator: EarClippingTriangulator,
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
        PARTICLE_BOX,
        JELLY,
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
    }

    private inner class TestInputAdapter : InputAdapter() {
        override fun keyUp(keycode: Int): Boolean {


            // Handle mode changing.
            when (keycode) {
                Input.Keys.ESCAPE -> Gdx.app.exit()
                Input.Keys.NUM_1 -> mode = Mode.BOX
                Input.Keys.NUM_2 -> mode = Mode.CIRCLE
                Input.Keys.NUM_3 -> mode = Mode.PARTICLE_BOX
                Input.Keys.NUM_4 -> mode = Mode.JELLY

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
                Mode.JELLY -> spawnCoolJelly(pos)
            }

            println("Clicked at: $pos")
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

    fun spawnCoolJelly(pos: Vector2,
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

    private fun createPlatforms() {
        val atlas = resources.mainAtlas

        // Test polygon rendering.
        engine.addEntity(Entity().apply {
            val region = atlas.findRegion("dev_grid")
            val vertices = floatArrayOf(
                    0f, 0f,
                    0f, 16f,
                    16f, 16f,
                    16f, 0f
            )

            val transform = Transform(
                    Vector2(-1f, 6f),
                    Vector2(1f, 1f))

            val body = world.createBody(BodyDef().apply {
                type = BodyType.DYNAMIC
            })
            body.createFixture(PolygonShape().apply {
                setAsBox(transform.width / 2, transform.height / 2)
            }, 1f)
            body.setTransform(transform.position.let { v ->
                Vec2(v.x, v.y)
            }, 0f)

            add(transform)
            add(Physics(body))
            add(PolygonRenderable(PolygonRegion(
                    region,
                    vertices,
                    earClippingTriangulator.computeTriangles(vertices).toArray())))
        })

        for (i in 1..2) {
            engine.addEntity(Entity().apply {
                add(Renderable(atlas.findRegion("dev_grid")))
                add(Transform(
                        Vector2(5f, 5f + i),
                        Vector2(i.toFloat(), i.toFloat())
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
        staticPlatform(-2f, 1.5f, 3f, 1f)
        staticPlatform(25f, 5f, 1f, 10f)
        staticPlatform(-25f, 5f, 1f, 10f)
    }
}