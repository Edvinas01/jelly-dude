package com.edd.jelly.behaviour.common.event

import com.edd.jelly.core.events.Event
import com.edd.jelly.core.tiled.JellyMap

data class LevelLoadedEvent(val map: JellyMap) : Event