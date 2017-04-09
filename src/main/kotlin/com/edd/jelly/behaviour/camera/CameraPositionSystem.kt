package com.edd.jelly.behaviour.camera

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.utils.ImmutableArray
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.Vector3
import com.edd.jelly.behaviour.components.transform
import com.edd.jelly.behaviour.level.LevelLoadedEvent
import com.edd.jelly.behaviour.player.Player
import com.edd.jelly.core.events.Messaging
import com.google.inject.Inject

class CameraPositionSystem @Inject constructor(
        private val messaging: Messaging,
        private val camera: OrthographicCamera
) : EntitySystem() {

    private lateinit var players: ImmutableArray<Entity>

    private companion object {

        /**
         * Speed of the camera which follows the player.
         */
        val CAMERA_SPEED = 2f
    }

    init {
        initListeners()
    }

    override fun addedToEngine(engine: Engine) {
        players = engine.getEntitiesFor(Family.all(Player::class.java).get())
    }

    override fun update(deltaTime: Float) {

        // If we have a player, follow the camera to it.
        players.firstOrNull()?.let {
            val transform = it.transform

            camera.position.lerp(Vector3(
                    transform.position.x,
                    transform.position.y,
                    0f
            ), deltaTime * CAMERA_SPEED)
        }

        camera.update()
    }

    private fun initListeners() {
        messaging.listen<LevelLoadedEvent> {
            // todo focus on point of interest instantly
        }
    }
}