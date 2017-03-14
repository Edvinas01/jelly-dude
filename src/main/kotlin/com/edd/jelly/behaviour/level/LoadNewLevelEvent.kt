package com.edd.jelly.behaviour.level

import com.edd.jelly.core.events.Event

data class LoadNewLevelEvent(
        val levelName: String
) : Event