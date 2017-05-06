package com.edd.jelly.behaviour.common.hook

import com.edd.jelly.behaviour.player.Player

interface MovementFunction {

    /**
     * Called before processing player movement.
     */
    fun beforeProcessMove(player: Player)

    /**
     * Called after processing player movement.
     */
    fun afterProcessMove(player: Player)
}