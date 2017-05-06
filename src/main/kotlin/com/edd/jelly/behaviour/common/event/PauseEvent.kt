package com.edd.jelly.behaviour.common.event

import com.edd.jelly.core.events.Event

data class PauseEvent(val pause: Boolean = false) : Event