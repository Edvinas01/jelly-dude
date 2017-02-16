package com.edd.jelly.behaviour.player

import com.badlogic.gdx.Input
import com.badlogic.gdx.InputAdapter

class PlayerInputAdapter(var player: Player) : InputAdapter() {

    private var deflateInitiated = false

    override fun keyDown(keycode: Int): Boolean {
        with (player) {
            when (keycode) {
                Input.Keys.W -> movingUp = true
                Input.Keys.S -> movingDown = true
                Input.Keys.A -> movingLeft = true
                Input.Keys.D -> movingRight = true
                Input.Keys.SPACE -> sticky = true
                Input.Keys.E -> {
                    deflateInitiated = true
                    deflationState = Player.Deflation.DEFLATE
                }
                else -> {
                    return false
                }
            }
        }
        return true
    }

    override fun keyUp(keycode: Int): Boolean {
        with(player) {
            when (keycode) {
                Input.Keys.W -> movingUp = false
                Input.Keys.S -> movingDown = false
                Input.Keys.A -> movingLeft = false
                Input.Keys.D -> movingRight = false
                Input.Keys.SPACE -> sticky = false
                Input.Keys.E -> {
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