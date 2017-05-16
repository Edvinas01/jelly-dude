package com.edd.jelly.util

import com.badlogic.gdx.audio.Music

@Suppress("TooManyFunctions") // Null object pattern / can't do much about music interface being huge.
object NullMusic : Music {

    override fun isPlaying() = false

    override fun isLooping() = false

    override fun setOnCompletionListener(listener: Music.OnCompletionListener?) {
    }

    override fun pause() {
    }

    override fun setPan(pan: Float, volume: Float) {
    }

    override fun getPosition() = 0f

    override fun setLooping(isLooping: Boolean) {
    }

    override fun getVolume() = 0f

    override fun play() {
    }

    override fun stop() {
    }

    override fun setVolume(volume: Float) {
    }

    override fun setPosition(position: Float) {
    }

    override fun dispose() {
    }
}