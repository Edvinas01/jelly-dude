package com.edd.jelly.behaviour.camera

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.utils.ImmutableArray
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.TimeUtils
import com.edd.jelly.behaviour.components.transform
import com.edd.jelly.behaviour.level.LevelLoadedEvent
import com.edd.jelly.behaviour.player.Player
import com.edd.jelly.core.events.Messaging
import com.google.inject.Inject
import java.util.*
import java.util.concurrent.TimeUnit

class CameraPositionSystem @Inject constructor(
        private val messaging: Messaging,
        private val camera: OrthographicCamera,
        private val random: Random
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

    init {
        initListeners()
    }

    override fun addedToEngine(engine: Engine) {
        players = engine.getEntitiesFor(Family.all(Player::class.java).get())
    }

    override fun update(deltaTime: Float) {
        val player = players.firstOrNull()
        if (player != null) {


            // If we have a player, follow the camera to it.
            val transform = player.transform
            moveCamera(deltaTime, transform.x, transform.y)
        } else {

            // Else move to a random focus point.
            with(getFocusPoint()) {
                moveCamera(deltaTime, x, y)
            }
        }
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
            focusPoints = map.focusPoints
            if (focusPoints.isNotEmpty()) {
                focusPoint = focusPoints[0]
                camera.position.set(focusPoint.x, focusPoint.y, 0f)
            }
        }
    }
}