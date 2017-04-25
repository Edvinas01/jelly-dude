package com.edd.jelly.behaviour.rendering

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.utils.ImmutableArray
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.edd.jelly.behaviour.components.Transform
import com.edd.jelly.behaviour.components.transform
import com.edd.jelly.behaviour.physics.LiquidRenderer
import com.edd.jelly.core.tiled.JellyMap
import com.edd.jelly.core.tiled.JellyMapRenderer
import com.edd.jelly.util.meters
import com.google.inject.Inject

class RenderingSystem @Inject constructor(
        private val tiledMapRenderer: JellyMapRenderer,
        private val liquidRenderer: LiquidRenderer,
        private val polygonBatch: PolygonSpriteBatch,
        private val spriteBatch: SpriteBatch,
        private val camera: OrthographicCamera
) : EntitySystem() {

    private lateinit var levels: ImmutableArray<Entity>
    private lateinit var simpleRenderableEntities: ImmutableArray<Entity>
    private lateinit var polygonRenderableEntities: ImmutableArray<Entity>
    private lateinit var softRenderableEntities: ImmutableArray<Entity>

    override fun addedToEngine(engine: Engine) {
        super.addedToEngine(engine)

        levels = engine.getEntitiesFor(Family.all(
                JellyMap::class.java
        ).get())

        simpleRenderableEntities = engine.getEntitiesFor(Family.all(
                Renderable::class.java,
                Transform::class.java
        ).get())

        polygonRenderableEntities = engine.getEntitiesFor(Family.all(
                PolygonRenderable::class.java,
                Transform::class.java
        ).get())

        softRenderableEntities = engine.getEntitiesFor(Family.all(
                SoftRenderable::class.java,
                Transform::class.java
        ).get())
    }

    override fun update(deltaTime: Float) {
        Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        // Seems that scene2d messes things up and this fixes it.
        // http://badlogicgames.com/forum/viewtopic.php?f=11&t=4672
        spriteBatch.setColor(1f, 1f, 1f, 1f)

        renderBackground()
        renderLiquid()
        renderEntities()
        renderSoft()
        renderPolygons()
        renderForeground()
    }

    /**
     * Render background and base map layers.
     */
    private fun renderBackground() {
        tiledMapRenderer.setView(camera)
        levels.forEach {
            tiledMapRenderer.drawBackground(JellyMap.mapper[it])
        }
    }

    /**
     * Render foreground layers.
     */
    private fun renderForeground() {
        levels.forEach {
            tiledMapRenderer.drawForeground(JellyMap.mapper[it])
        }
    }

    /**
     * Render simple entities.
     */
    private fun renderEntities() {
        spriteBatch.draw {
            for (entity in simpleRenderableEntities) {
                val transform = entity.transform
                val region = Renderable[entity].textureRegion

                val width =
                        if (transform.width > 0) transform.width
                        else region.regionWidth.meters

                val height =
                        if (transform.height > 0) transform.height
                        else region.regionHeight.meters

                val halfWidth = width / 2
                val halfHeight = height / 2

                it.draw(
                        region,
                        transform.x - halfWidth,
                        transform.y - halfHeight,
                        halfWidth,
                        halfHeight,
                        width,
                        height,
                        1f,
                        1f,
                        transform.rotation)
            }
        }
    }

    /**
     * Render polygon entities.
     */
    private fun renderPolygons() {
        polygonBatch.draw {
            for (entity in polygonRenderableEntities) {
                val polygonRenderable = PolygonRenderable.mapper[entity]
                val polygonRegion = polygonRenderable.polygonRegion
                val transform = entity.transform
                val region = polygonRegion.region

                val width =
                        if (transform.width > 0) transform.width
                        else region.regionWidth.meters

                val height =
                        if (transform.height > 0) transform.height
                        else region.regionHeight.meters

                val halfWidth = width / 2
                val halfHeight = height / 2

                it.draw(
                        polygonRegion,
                        transform.x - halfWidth,
                        transform.y - halfHeight,
                        halfWidth,
                        halfHeight,
                        transform.width,
                        transform.height,
                        1f,
                        1f,
                        transform.rotation)
            }
        }
    }

    /**
     * Render soft polygons.
     */
    private fun renderSoft() {
        polygonBatch.draw { b ->
            for (entity in softRenderableEntities) {
                val transform = entity.transform
                val region = SoftRenderable[entity].region
                val tex = region.region

                // Soft bodies do not have rotation and do not support
                // resizing / scaling.
                b.draw(
                        region,
                        transform.x,
                        transform.y,
                        tex.regionWidth.toFloat(),
                        tex.regionHeight.toFloat()
                )
            }
        }
    }

    /**
     * Render liquid fun particles.
     */
    private fun renderLiquid() {
        liquidRenderer.render(camera)
    }

    /**
     * Helper function to wrap block withing sprite batch begin and end statements.
     */
    private inline fun <T : Batch> T.draw(block: (T) -> Unit) {
        this.projectionMatrix = camera.combined
        begin()
        block(this)
        end()
    }
}