package com.edd.jelly.systems

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.PolygonShape
import com.badlogic.gdx.physics.box2d.World
import com.edd.jelly.components.Physics
import com.edd.jelly.components.Renderable
import com.edd.jelly.components.Transform
import com.edd.jelly.components.transform
import com.edd.jelly.util.resources.ResourceManager
import com.google.inject.Inject

class TestSystem @Inject constructor(
        private val resources: ResourceManager,
        private val world: World
) : IteratingSystem(Family.all(
        Transform::class.java,
        Physics::class.java
).get()) {

    override fun addedToEngine(engine: Engine) {
        super.addedToEngine(engine)

        val atlas = resources.devAtlas

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

    override fun processEntity(entity: Entity, deltaTime: Float) {
    }
}