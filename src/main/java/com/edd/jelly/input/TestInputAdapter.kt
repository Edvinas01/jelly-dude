package com.edd.jelly.input

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.PolygonShape
import com.badlogic.gdx.physics.box2d.World
import com.edd.jelly.components.Physics
import com.edd.jelly.components.Renderable
import com.edd.jelly.components.Transform
import com.edd.jelly.components.transform
import com.edd.jelly.util.resources.ResourceManager
import com.edd.jelly.util.resources.get
import com.google.inject.Inject

class TestInputAdapter @Inject constructor(
        private val resources: ResourceManager,
        private val camera: Camera,
        private val engine: Engine,
        private val world: World
) : InputAdapter() {

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        val pos = camera.unproject(Vector3(screenX.toFloat(), screenY.toFloat(), 0f))

        engine.addEntity(Entity().apply {
            add(Renderable(resources.devAtlas["the_borker"]))
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
        return true
    }
}