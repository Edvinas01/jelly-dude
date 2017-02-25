package com.edd.jelly.behaviour.player

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.InputAdapter
import com.edd.jelly.core.events.Messaging

class PlayerInputAdapter(
        val messaging: Messaging,
        val player: Entity
) : InputAdapter() {

    override fun keyDown(keycode: Int): Boolean {
        with(Player.mapper[player]) {
            when (keycode) {
                Keys.W -> movingUp = true
                Keys.S -> movingDown = true
                Keys.A -> movingLeft = true
                Keys.D -> movingRight = true
                Keys.SPACE -> sticky = true
                Keys.E -> {
                    deflateInitiated = true
                    deflationState = Player.Deflation.DEFLATE
                }
                Keys.R -> {

                    // Send input events that fire off immediately.
                    messaging.send(PlayerInputEvent(
                            player,
                            Keys.R == keycode
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
        with(Player.mapper[player]) {
            when (keycode) {
                Keys.W -> movingUp = false
                Keys.S -> movingDown = false
                Keys.A -> movingLeft = false
                Keys.D -> movingRight = false
                Keys.SPACE -> sticky = false
                Keys.E -> {
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
}