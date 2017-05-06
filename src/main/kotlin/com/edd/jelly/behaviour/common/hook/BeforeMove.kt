package com.edd.jelly.behaviour.common.hook

import com.edd.jelly.behaviour.player.Player

interface BeforeMove {

    /**
     * Called before processing player movement.
     */
    fun beforeMove(player: Player)
}