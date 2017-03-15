package com.edd.jelly.behaviour.player

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