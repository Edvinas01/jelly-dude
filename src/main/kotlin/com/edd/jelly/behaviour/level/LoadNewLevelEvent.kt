package com.edd.jelly.behaviour.level

import com.edd.jelly.core.events.Event

data class LoadNewLevelEvent(
        val name: String,
        val internal: Boolean = false
) : Event