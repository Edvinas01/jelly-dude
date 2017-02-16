package com.edd.jelly.behaviour.player

import com.badlogic.gdx.Input
import com.badlogic.gdx.InputAdapter

class PlayerInputAdapter(private val player: Player) : InputAdapter() {

    override fun keyDown(keycode: Int): Boolean {
        when (keycode) {
            Input.Keys.W -> player.movingUp = true
            Input.Keys.S -> player.movingDown = true
            Input.Keys.A -> player.movingLeft = true
            Input.Keys.D -> player.movingRight = true
            Input.Keys.E -> player.stick = true
            Input.Keys.Q -> player.deflationState = Player.Deflation.DEFLATE
            else -> {
                return false
            }
        }
        return true
    }

    override fun keyUp(keycode: Int): Boolean {
        when (keycode) {
            Input.Keys.W -> player.movingUp = false
            Input.Keys.S -> player.movingDown = false
            Input.Keys.A -> player.movingLeft = false
            Input.Keys.D -> player.movingRight = false
            Input.Keys.E -> player.stick = false
            Input.Keys.Q -> player.deflationState = Player.Deflation.INFLATE
            else -> {
                return false
            }
        }
        return true
    }
}