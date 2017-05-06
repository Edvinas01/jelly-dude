package com.edd.jelly.behaviour.player

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.InputAdapter
import com.edd.jelly.core.events.Messaging
import com.edd.jelly.behaviour.common.event.PlayerInputEvent
import com.edd.jelly.core.configuration.Config

class PlayerInputAdapter(
        val messaging: Messaging
) : InputAdapter() {

    private var upKey = Keys.W
    private var downKey = Keys.S
    private var leftKey = Keys.A
    private var rightKey = Keys.D
    private var stickKey = Keys.SPACE
    private var shrinkKey = Keys.E
    private var resetKey = Keys.R

    var disabled = true
    var player: Entity? = null

    override fun keyDown(keycode: Int): Boolean {
        if (disabled || player == null) {
            return false
        }

        with(Player.mapper[player]) {
            when (keycode) {
                upKey -> movingUp = true
                downKey -> movingDown = true
                leftKey -> movingLeft = true
                rightKey -> movingRight = true
                stickKey -> sticky = true
                shrinkKey -> {
                    deflateInitiated = true
                    deflationState = Player.Deflation.DEFLATE
                }
                resetKey -> {
                    reset = true

                    // Send input events that fire off immediately.
                    messaging.send(PlayerInputEvent(
                            player!!,
                            resetKey == keycode
                    ))
                }
                else -> {
                    return false
                }
            }
        }
        return true
    }

    override fun keyUp(keycode: Int): Boolean {
        if (disabled || player == null) {
            return false
        }

        with(Player.mapper[player]) {
            when (keycode) {
                upKey -> movingUp = false
                downKey -> movingDown = false
                leftKey -> movingLeft = false
                rightKey -> movingRight = false
                stickKey -> sticky = false
                shrinkKey -> {
                    if (deflateInitiated) {
                        deflateInitiated = false
                        deflationState = Player.Deflation.INFLATE
                    }
                }
                else -> {
                    return false
                }
            }
            return true
        }
    }

    fun adaptInputs(input: Config.Input) {
        upKey = Keys.valueOf(input.up)
        downKey = Keys.valueOf(input.down)
        leftKey = Keys.valueOf(input.left)
        rightKey = Keys.valueOf(input.right)
        stickKey = Keys.valueOf(input.stick)
        shrinkKey = Keys.valueOf(input.shrink)
        resetKey = Keys.valueOf(input.reset)
    }
}