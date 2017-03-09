package com.edd.jelly.behaviour.level

import com.edd.jelly.core.events.Event

data class LevelLoadedEvent(val x: Float,
                            val y: Float) : Event