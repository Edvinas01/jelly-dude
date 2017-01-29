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
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.CircleShape
import com.badlogic.gdx.physics.box2d.PolygonShape
import com.badlogic.gdx.physics.box2d.World
import com.edd.jelly.components.Physics
import com.edd.jelly.components.Renderable
import com.edd.jelly.components.Transform
import com.edd.jelly.components.transform
import com.edd.jelly.util.resources.ResourceManager
import com.edd.jelly.util.resources.get
import com.google.inject.Inject

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
        CIRCLE
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

                else -> {
                    return false
                }
            }

            println("Mode: $mode")
            return true
        }

        override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
            val pos = camera.unproject(Vector3(screenX.toFloat(), screenY.toFloat(), 0f)).let { v ->
                Vector2(v.x, v.y)
            }

            when (mode) {
                Mode.BOX -> spawnBox(pos)
                Mode.CIRCLE -> spawnCircle(pos)
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
                    type = BodyDef.BodyType.DynamicBody
                })

                body.createFixture(PolygonShape().apply {
                    setAsBox(transform.width / 2, transform.height / 2)
                }, 1f)
                body.setTransform(transform.position, 0f)

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
                    type = BodyDef.BodyType.DynamicBody
                })

                body.createFixture(CircleShape().apply {
                    this.radius = transform.width / 2
                }, 1f)
                body.setTransform(transform.position, 0f)

                add(Physics(body))
            })
        }
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
                type = BodyDef.BodyType.DynamicBody
            })

            body.createFixture(PolygonShape().apply {
                setAsBox(transform.width / 2, transform.height / 2)
            }, 1f)
            body.setTransform(transform.position, 0f)

            add(Physics(body))
        })

        engine.addEntity(Entity().apply {
            add(Renderable(atlas.findRegion("dev_grid_tiny")))
            add(Transform(
                    Vector2(0f, 0f),
                    Vector2(100f, 0.5f)
            ))

            val body = world.createBody(BodyDef().apply {
                type = BodyDef.BodyType.StaticBody
            })

            body.createFixture(PolygonShape().apply {
                setAsBox(transform.width / 2, transform.height / 2)
            }, 1f)
            body.setTransform(transform.position, 0f)

            add(Physics(body))
        })
    }
}