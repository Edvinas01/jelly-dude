package com.edd.jelly.behaviour.common.event

import com.edd.jelly.core.events.Event

data class PlayMusicEvent(
        val name: String,
        val loop: Boolean = false
) : Event