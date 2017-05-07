package com.edd.jelly.behaviour.common.hook

import com.edd.jelly.behaviour.player.Player

interface BeforeStick {

    fun beforeStick(player: Player): Boolean
}