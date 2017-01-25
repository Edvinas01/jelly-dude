package com.edd.jelly.game.systems

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.PolygonShape
import com.badlogic.gdx.physics.box2d.World
import com.edd.jelly.game.components.Physics
import com.edd.jelly.game.components.Renderable
import com.edd.jelly.game.components.Transform
import com.edd.jelly.game.components.transform
import com.google.inject.Inject

class TestSystem @Inject constructor(private val world: World) : IteratingSystem(Family.all(
        Transform::class.java,
        Physics::class.java).get()) {

    override fun addedToEngine(engine: Engine) {
        super.addedToEngine(engine)

        val atlas = TextureAtlas(Gdx.files.internal("textures/dev.atlas"))

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