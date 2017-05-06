package com.edd.jelly.behaviour.common.event

import com.edd.jelly.core.events.Event

data class LoadNewLevelEvent(
        val name: String,
        val internal: Boolean = false
) : Event