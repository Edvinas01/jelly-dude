package com.edd.jelly.behaviour.player

interface HealthFunction {

    /**
     * Called before processing player health tick.
     *
     * @return true if health tick should occur or false otherwise.
     */
    fun beforeHealthTick(player: Player, moved: Float): Boolean
}