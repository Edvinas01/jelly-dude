package com.edd.jelly.core.configuration

import com.edd.jelly.core.events.Event

data class ConfigChangedEvent(val config: Config) : Event