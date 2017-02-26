package com.edd.jelly.behaviour.level

import com.edd.jelly.core.events.Event

data class LoadLevelEvent(
        val levelName: String
) : Event