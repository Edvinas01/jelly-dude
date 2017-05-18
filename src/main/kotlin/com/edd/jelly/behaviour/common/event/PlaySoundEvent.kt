package com.edd.jelly.behaviour.common.event

import com.edd.jelly.core.events.Event

data class PlaySoundEvent(
        val name: String,
        val lowPitch: Float = 1f,
        val highPitch: Float = 1f,
        val loop: Boolean = false,
        val volumeMultiplier: Float = 1f
) : Event