package com.edd.jelly.behaviour.debug

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.utils.ImmutableArray
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.edd.jelly.behaviour.components.Transform
import com.edd.jelly.behaviour.components.transform
import com.edd.jelly.behaviour.physics.DebugRenderer
import com.edd.jelly.core.configuration.ConfigChangedEvent
import com.edd.jelly.core.configuration.Configurations
import com.edd.jelly.core.events.Messaging
import com.edd.jelly.core.resources.ResourceManager
import com.edd.jelly.util.meters
import com.google.inject.Inject
import org.jbox2d.common.MathUtils
import org.jbox2d.dynamics.World

class DebugRenderingSystem @Inject constructor(
        private val spriteBatch: SpriteBatch,
        private val messaging: Messaging,
        private val renderer: DebugRenderer,
        private val camera: OrthographicCamera,
        private val world: World,
        resourceManager: ResourceManager,
        configurations: Configurations
) : EntitySystem() {

    private val transformTexture = resourceManager.getTexture("xy_axis")
    private val game = configurations.config.game

    private lateinit var transforms: ImmutableArray<Entity>

    override fun addedToEngine(engine: Engine) {
        transforms = engine.getEntitiesFor(Family.all(
                Transform::class.java
        ).get())

        messaging.listen<ConfigChangedEvent> {
            setProcessing(it.config.game.debug)
        }

        setProcessing(game.debug)
    }

    override fun update(deltaTime: Float) {
        renderer.render(world, camera.combined)

        renderTransforms()
    }

    private fun renderTransforms() {
        spriteBatch.projectionMatrix = camera.combined
        spriteBatch.begin()
        transforms.forEach {
            val transform = it.transform

            spriteBatch.draw(
                    transformTexture,
                    transform.x,
                    transform.y,
                    MathUtils.max(transformTexture.width.meters, transform.width),
                    MathUtils.max(transformTexture.height.meters, transform.height)
            )
        }
        spriteBatch.end()
    }
}