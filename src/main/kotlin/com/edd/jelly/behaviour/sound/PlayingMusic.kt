package com.edd.jelly.behaviour.sound

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.audio.Music
import com.edd.jelly.util.ComponentResolver

data class PlayingMusic(val music: Music) : Component {

    companion object : ComponentResolver<PlayingMusic>(PlayingMusic::class.java)
}