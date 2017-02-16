package com.edd.jelly.behaviour.test

import com.badlogic.ashley.core.EntitySystem
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.google.inject.Inject

/**
 * Smoothly moves the camera around.
 */
class CameraControllerSystem @Inject constructor(
        multiplexer: InputMultiplexer,
        private val camera: Camera
) : EntitySystem() {

    private var enabled = false
    private var right = false
    private var left = false
    private var up = false
    private var down = false

    private var zoomIn = false
    private var zoomOut = false

    private val moveVector = Vector2()
    private var zoomVector = 0f

    companion object {
        val SLOW_DOWN_SPEED = 5f
        val MOVE_SPEED = 0.8f
        val ZOOM_SPEED = 0.005f
    }

    init {
        multiplexer.addProcessor(object : InputAdapter() {
            override fun scrolled(amount: Int): Boolean {
                return false
            }

            override fun keyDown(keycode: Int): Boolean {
                when (keycode) {
                    Input.Keys.RIGHT -> right = true
                    Input.Keys.LEFT -> left = true
                    Input.Keys.UP -> up = true
                    Input.Keys.DOWN -> down = true
                    Input.Keys.PAGE_UP -> zoomIn = true
                    Input.Keys.PAGE_DOWN -> zoomOut = true
                    else -> return false // Input not handled.
                }
                return true
            }

            override fun keyUp(keycode: Int): Boolean {
                when (keycode) {
                    Input.Keys.GRAVE -> enabled = !enabled
                    Input.Keys.RIGHT -> right = false
                    Input.Keys.LEFT -> left = false
                    Input.Keys.UP -> up = false
                    Input.Keys.DOWN -> down = false
                    Input.Keys.PAGE_UP -> zoomIn = false
                    Input.Keys.PAGE_DOWN -> zoomOut = false
                    else -> return false
                }
                return true
            }
        })
    }

    override fun update(deltaTime: Float) {
        if (!enabled) {
            return
        }

        val slowDown = deltaTime * SLOW_DOWN_SPEED

        moveVector.lerp(Vector2.Zero, slowDown)
        zoomVector = MathUtils.lerp(zoomVector, 0f, slowDown)

        // Add speed to movement vector.
        if (right) {
            moveVector.x += MOVE_SPEED
        }
        if (left) {
            moveVector.x -= MOVE_SPEED
        }
        if (up) {
            moveVector.y += MOVE_SPEED
        }
        if (down) {
            moveVector.y -= MOVE_SPEED
        }

        // Control camera zoom vector.
        if (zoomIn) {
            zoomVector -= ZOOM_SPEED
        }
        if (zoomOut) {
            zoomVector += ZOOM_SPEED
        }

        if (camera is OrthographicCamera) {
            camera.zoom += zoomVector
        }

        camera.position.lerp(Vector3(
                camera.position.x + moveVector.x,
                camera.position.y + moveVector.y,
                0f
        ), deltaTime)
        camera.update()
    }
}