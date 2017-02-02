package com.edd.jelly.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.Batch
import com.edd.jelly.components.Renderable
import com.edd.jelly.components.Transform
import com.edd.jelly.components.renderable
import com.edd.jelly.components.transform
import com.edd.jelly.util.meters
import com.google.inject.Inject

class RenderingSystem @Inject constructor(private val camera: Camera,
                                          private val batch: Batch) : IteratingSystem(Family.all(

        Transform::class.java,
        Renderable::class.java).get()) {

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

        val width =
                if (transform.width > 0) transform.width
                else region.regionWidth.toFloat().meters

        val height =
                if (transform.height > 0) transform.height
                else region.regionHeight.toFloat().meters

        val halfWidth = width / 2
        val halfHeight = height / 2

        batch.draw(region,
                transform.x - halfWidth,
                transform.y - halfHeight,
                halfWidth,
                halfHeight,
                width,
                height,
                transform.scaleX,
                transform.scaleY,
                transform.rotation)
    }
}