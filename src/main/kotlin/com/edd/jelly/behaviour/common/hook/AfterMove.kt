package com.edd.jelly.behaviour.common.hook

import com.edd.jelly.behaviour.player.Player

interface AfterMove {

    /**
     * Called after processing player movement.
     */
    fun afterMove(player: Player)
}