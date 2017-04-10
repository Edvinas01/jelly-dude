package com.edd.jelly.behaviour.pause

import com.edd.jelly.core.events.Event

data class PauseEvent(val pause: Boolean = false) : Event