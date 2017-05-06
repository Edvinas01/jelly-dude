package com.edd.jelly.behaviour.camera

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.utils.ImmutableArray
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.TimeUtils
import com.edd.jelly.behaviour.position.transform
import com.edd.jelly.behaviour.common.event.LevelLoadedEvent
import com.edd.jelly.behaviour.player.Player
import com.edd.jelly.behaviour.common.event.ConfigChangedEvent
import com.edd.jelly.core.configuration.Configurations
import com.edd.jelly.core.events.Messaging
import com.google.inject.Inject
import java.util.Random
import java.util.concurrent.TimeUnit

class CameraPositionSystem @Inject constructor(
        private val messaging: Messaging,
        private val camera: OrthographicCamera,
        private val random: Random,
        configurations: Configurations
) : EntitySystem() {

    private companion object {

        /**
         * Speed of the camera which follows the player.
         */
        val CAMERA_SPEED = 2f

        /**
         * How often to change focus points.
         */
        val CHANGE_POINT_TIME = TimeUnit.SECONDS.toMillis(15)
    }

    private lateinit var players: ImmutableArray<Entity>

    /**
     * List of available focus points.
     */
    private var focusPoints: List<Vector2> = emptyList()

    /**
     * Current focus point.
     */
    private var focusPoint: Vector2 = Vector2()

    /**
     * When was the last that that we moved to a point.
     */
    private var movedPoint = 0L

    private var worldWidth = 0f
    private var worldHeight = 0f

    private var followPlayer = !configurations.config.game.debug

    init {
        initListeners()
    }

    override fun addedToEngine(engine: Engine) {
        players = engine.getEntitiesFor(Family.all(Player::class.java).get())
    }

    override fun update(deltaTime: Float) {
        val player = players.firstOrNull()
        if (player != null) {

            // If we have a player and debug is off, follow the camera to it.
            if (followPlayer) {
                val transform = player.transform
                moveCamera(deltaTime, transform.x, transform.y)
            }

        } else {

            // Else move to a random focus point.
            with(getFocusPoint()) {
                moveCamera(deltaTime, x, y)
            }
        }

        // Keep camera in world bounds.
        val hw = camera.viewportWidth / 2
        val hh = camera.viewportHeight / 2

        camera.position.x = MathUtils.clamp(camera.position.x, hw, worldWidth - hw)
        camera.position.y = MathUtils.clamp(camera.position.y, hh, worldHeight - hh)

        camera.update()
    }

    /**
     * Get current or new focus point.
     */
    private fun getFocusPoint(): Vector2 {
        if (TimeUtils.timeSinceMillis(movedPoint) > CHANGE_POINT_TIME) {
            movedPoint = TimeUtils.millis()

            if (focusPoints.isNotEmpty()) {
                focusPoint = focusPoints[random.nextInt(focusPoints.size)]
            }
        }
        return focusPoint
    }

    /**
     * Move camera to the specified position.
     */
    private fun moveCamera(dt: Float, x: Float, y: Float) {
        camera.position.lerp(Vector3(x, y, 0f), dt * CAMERA_SPEED)
    }

    private fun initListeners() {
        messaging.listen<LevelLoadedEvent> { (map) ->
            worldWidth = map.width
            worldHeight = map.height

            focusPoints = map.focusPoints
            if (focusPoints.isNotEmpty()) {
                focusPoint = focusPoints[0]
                camera.position.set(focusPoint.x, focusPoint.y, 0f)
            }
        }

        messaging.listen<ConfigChangedEvent> { (config) ->
            followPlayer = !config.game.debug
        }
    }
}