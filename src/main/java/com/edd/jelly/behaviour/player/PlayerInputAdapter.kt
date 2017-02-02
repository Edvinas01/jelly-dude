package com.edd.jelly.behaviour.player

import com.badlogic.gdx.Input
import com.badlogic.gdx.InputAdapter
import com.edd.jelly.components.PlayerComponent

class PlayerInputAdapter(private val player: PlayerComponent) : InputAdapter() {

    override fun keyDown(keycode: Int): Boolean {
        when (keycode) {
            Input.Keys.W -> player.movingUp = true
            Input.Keys.S -> player.movingDown = true
            Input.Keys.A -> player.movingLeft = true
            Input.Keys.D -> player.movingRight = true
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
            else -> {
                return false
            }
        }
        return true
    }
}