package com.edd.jelly.behaviour.test

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Vector3
import com.edd.jelly.game.GuiCamera
import com.edd.jelly.util.plusAssign
import com.google.inject.Inject

// Allows to draw lines n stuff, for presentation.
class PixelDrawingSystem @Inject constructor(
        private val spriteBatch: SpriteBatch,

        @GuiCamera
        private val camera: OrthographicCamera,
        private val inputMultiplexer: InputMultiplexer

) : EntitySystem() {

    private companion object {
        const val DRAW_SIZE = 3
    }

    private val pixmap = Pixmap(
            Gdx.graphics.width,
            Gdx.graphics.height,
            Pixmap.Format.RGBA8888
    )

    private val texture =
            Texture(pixmap, Pixmap.Format.RGBA8888, false)

    // Don't want to keep creating vectors on runtime.
    private val direction = Vector3()
    private val tmp = Vector3()
    private val end = Vector3()

    private var draw = false

    inner class DrawingInputAdapter : InputAdapter() {

        // Enable drawing and also setup pixel map.
        override fun keyDown(keycode: Int) = if (Input.Keys.CONTROL_LEFT == keycode) {
            pixmap.setColor(Color.RED)
            draw = true
            true
        } else {
            false
        }

        // Disable drawing and also clear texture / pixel map.
        override fun keyUp(keycode: Int) = if (Input.Keys.CONTROL_LEFT == keycode) {
            pixmap.setColor(Color.CLEAR)
            pixmap.fill()
            texture.draw(pixmap, 0, 0)
            draw = false
            true
        } else {
            false
        }

        override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
            if (draw) {
                drawAndFlush(screenX, screenY)
                refreshEndpoint(screenX, screenY)
                return true
            }
            return false
        }

        // Draw!
        override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
            if (draw) {

                tmp.set(screenX.toFloat(), screenY.toFloat(), 0f)
                if (!end.isZero) {
                    direction.set(end.x - screenX, end.y - screenY, 0f).nor()

                    val step = 1f

                    // Remaining distance.
                    var dst = DRAW_SIZE + 1f
                    while (dst > step) {

                        // Move start towards the end.
                        tmp += direction.scl(step)

                        // Draw stuff while traveling.
                        draw(tmp)

                        // Get remaining length.
                        dst = tmp.dst(end)
                    }
                }

                drawAndFlush(screenX, screenY)
                refreshEndpoint(screenX, screenY)
                return true
            }
            return false
        }
    }

    override fun addedToEngine(engine: Engine) {
        inputMultiplexer.addProcessor(DrawingInputAdapter())
    }

    override fun update(deltaTime: Float) {
        spriteBatch.projectionMatrix = camera.combined

        spriteBatch.begin()
        spriteBatch.draw(texture, 0f, 0f)
        spriteBatch.end()
    }

    private fun drawAndFlush(x : Number, y : Number) {
        draw(x, y)
        texture.draw(pixmap, 0, 0)
    }

    private fun draw(vec: Vector3) {
        draw(vec.x, vec.y)
    }

    private fun draw(x : Number, y : Number) {
        pixmap.fillCircle(x.toInt(), y.toInt(), DRAW_SIZE)
    }

    private fun refreshEndpoint(x : Number, y : Number) {
        end.set(x.toFloat(), y.toFloat(), 0f)
    }
}