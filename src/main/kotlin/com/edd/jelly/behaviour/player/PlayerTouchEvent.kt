package com.edd.jelly.behaviour.player

import com.edd.jelly.core.events.Event
import org.jbox2d.dynamics.Body

data class PlayerTouchEvent(
        val player: Player,
        val touched: Body
) : Event