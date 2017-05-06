package com.edd.jelly.behaviour.common.event

import com.edd.jelly.core.configuration.Config
import com.edd.jelly.core.events.Event

data class ConfigChangedEvent(val config: Config) : Event