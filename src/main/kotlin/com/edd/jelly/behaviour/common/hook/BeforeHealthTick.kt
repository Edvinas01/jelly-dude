package com.edd.jelly.behaviour.common.hook

import com.edd.jelly.behaviour.player.Player

interface BeforeHealthTick {

    /**
     * Called before processing player health tick.
     *
     * @return true if health tick should occur or false otherwise.
     */
    fun beforeHealthTick(player: Player, moved: Float): Boolean
}