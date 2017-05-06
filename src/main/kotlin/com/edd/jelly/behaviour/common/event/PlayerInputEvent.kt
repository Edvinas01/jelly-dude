package com.edd.jelly.behaviour.common.event

import com.badlogic.ashley.core.Entity
import com.edd.jelly.core.events.Event

data class PlayerInputEvent(
        val player: Entity,
        val reset: Boolean = false
) : Event