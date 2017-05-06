package com.edd.jelly.behaviour.debug

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.utils.ImmutableArray
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.edd.jelly.behaviour.position.Transform
import com.edd.jelly.behaviour.position.transform
import com.edd.jelly.behaviour.physics.PhysicsDebugRenderer
import com.edd.jelly.behaviour.common.event.ConfigChangedEvent
import com.edd.jelly.core.configuration.Configurations
import com.edd.jelly.core.events.Messaging
import com.edd.jelly.core.resources.ResourceManager
import com.edd.jelly.core.resources.get
import com.edd.jelly.util.meters
import com.google.inject.Inject
import org.jbox2d.common.MathUtils
import org.jbox2d.dynamics.World

class DebugRenderingSystem @Inject constructor(
        private val spriteBatch: SpriteBatch,
        private val messaging: Messaging,
        private val renderer: PhysicsDebugRenderer,
        private val camera: OrthographicCamera,
        private val world: World,
        resourceManager: ResourceManager,
        configurations: Configurations
) : EntitySystem() {

    private val transformTexture = resourceManager.mainAtlas["xy_axis"]!!
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
                    MathUtils.max(transformTexture.regionWidth.meters, transform.width),
                    MathUtils.max(transformTexture.regionHeight.meters, transform.height)
            )
        }
        spriteBatch.end()
    }
}