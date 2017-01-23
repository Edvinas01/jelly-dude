package com.edd.jelly.game.render

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.math.Vector2
import com.edd.jelly.game.component.Renderable
import com.edd.jelly.game.component.Transform
import com.edd.jelly.game.component.renderable
import com.edd.jelly.game.component.transform
import com.edd.jelly.util.meters
import com.google.inject.Inject

class RenderingSystem @Inject constructor(private val camera: Camera,
                                          private val batch: Batch) : IteratingSystem(Family.all(

        Transform::class.java,
        Renderable::class.java).get()) {

    override fun addedToEngine(engine: Engine) {
        super.addedToEngine(engine)

        val atlas = TextureAtlas(Gdx.files.internal("textures/dev.atlas"))

        engine.addEntity(Entity().apply {
            add(Transform(
                    Vector2(0f, 0f),
                    Vector2(256f, 256f),
                    Vector2(1f, 1f),
                    5f
            ))
            add(Renderable(atlas.findRegion("the_borker")))
        })

        engine.addEntity(Entity().apply {
            add(Transform(
                    Vector2(0f, 0f)
            ))
            add(Renderable(atlas.findRegion("dev_grid")))
        })

        engine.addEntity(Entity().apply {
            add(Transform(
                    Vector2(0f, 256f)
            ))
            add(Renderable(atlas.findRegion("dev_grid_tiny")))
        })
    }

    override fun update(deltaTime: Float) {
        batch.projectionMatrix = camera.combined

        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        batch.begin()
        super.update(deltaTime)
        batch.end()
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val transform = entity.transform
        val region = entity.renderable.textureRegion

        val width = (
                if (transform.width > 0) transform.width
                else region.regionWidth.toFloat())
                .meters

        val height = (
                if (transform.height > 0) transform.height
                else region.regionHeight.toFloat())
                .meters

        batch.draw(region,
                transform.x,
                transform.y,
                width / 2,
                height / 2,
                width,
                height,
                transform.scaleX,
                transform.scaleY,
                transform.rotation)
    }
}