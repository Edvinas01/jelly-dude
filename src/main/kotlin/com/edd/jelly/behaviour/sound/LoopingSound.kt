package com.edd.jelly.behaviour.sound

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.audio.Sound
import com.edd.jelly.util.ComponentResolver

data class LoopingSound(val id: Long, val sound: Sound) : Component {

    companion object : ComponentResolver<LoopingSound>(LoopingSound::class.java)
}