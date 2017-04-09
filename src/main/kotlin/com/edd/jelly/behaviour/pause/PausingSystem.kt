package com.edd.jelly.behaviour.pause

/**
 * Marks entity systems which can be paused.
 */
interface PausingSystem {

    /**
     * Callback method which is fired once the system is paused.
     */
    fun paused(pause: Boolean = true) {
    }
}