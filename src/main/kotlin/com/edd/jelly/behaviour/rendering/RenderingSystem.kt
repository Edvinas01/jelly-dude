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
import com.edd.jelly.behaviour.level.RenderableLevel
import com.edd.jelly.util.meters
import com.google.inject.Inject

class RenderingSystem @Inject constructor(
        private val tiledMapRenderer: LayeredTiledMapRenderer,
        private val polygonBatch: PolygonSpriteBatch,
        private val spriteBatch: SpriteBatch,
        private val camera: OrthographicCamera
) : EntitySystem() {

    private lateinit var levelRenderableEntities: ImmutableArray<Entity>
    private lateinit var simpleRenderableEntities: ImmutableArray<Entity>
    private lateinit var polygonRenderableEntities: ImmutableArray<Entity>

    override fun addedToEngine(engine: Engine) {
        super.addedToEngine(engine)

        levelRenderableEntities = engine.getEntitiesFor(Family.all(
                RenderableLevel::class.java
        ).get())

        simpleRenderableEntities = engine.getEntitiesFor(Family.all(
                Renderable::class.java,
                Transform::class.java
        ).get())

        polygonRenderableEntities = engine.getEntitiesFor(Family.all(
                PolygonRenderable::class.java,
                Transform::class.java
        ).get())
    }

    override fun update(deltaTime: Float) {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        renderBackground()
        renderEntities()
        renderPolygons()
        renderForeground()
    }

    /**
     * Render foreground layers.
     */
    fun renderForeground() {
        levelRenderableEntities.forEach {
            tiledMapRenderer.render(RenderableLevel.mapper[it].foregroundLayers)
        }
    }

    /**
     * Render background and base map layers.
     */
    fun renderBackground() {
        tiledMapRenderer.setView(camera)
        levelRenderableEntities.forEach {
            with(RenderableLevel.mapper[it]) {
                background?.let { texture ->
                    spriteBatch.draw {
                        it.draw(
                                texture,
                                camera.position.x - camera.viewportWidth / 2,
                                camera.position.y - camera.viewportHeight / 2,
                                camera.viewportWidth,
                                camera.viewportHeight
                        )
                    }
                }
                tiledMapRenderer.render(backgroundLayers)
                tiledMapRenderer.render(baseLayers)
            }
        }
    }

    /**
     * Render simple entities.
     */
    fun renderEntities() {
        spriteBatch.draw {
            for (entity in simpleRenderableEntities) {
                val transform = entity.transform
                val region = Renderable.mapper[entity].textureRegion

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
    fun renderPolygons() {
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
     * Helper function to wrap block withing sprite batch begin and end statements.
     */
    private inline fun <T : Batch> T.draw(block: (T) -> Unit) {
        this.projectionMatrix = camera.combined
        begin()
        block(this)
        end()
    }
}