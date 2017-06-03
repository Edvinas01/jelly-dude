package com.edd.jelly.util

import com.badlogic.gdx.audio.Sound

object NullSound : Sound {

    override fun pause() {
    }

    override fun pause(soundId: Long) {
    }

    override fun setPitch(soundId: Long, pitch: Float) {
    }

    override fun setPan(soundId: Long, pan: Float, volume: Float) {
    }

    override fun setLooping(soundId: Long, looping: Boolean) {
    }

    override fun play() = -1L

    override fun play(volume: Float) = -1L

    override fun play(volume: Float, pitch: Float, pan: Float) = -1L

    override fun stop() {
    }

    override fun stop(soundId: Long) {
    }

    override fun setVolume(soundId: Long, volume: Float) {
    }

    override fun resume() {
    }

    override fun resume(soundId: Long) {
    }

    override fun loop() = -1L

    override fun loop(volume: Float) = -1L

    override fun loop(volume: Float, pitch: Float, pan: Float) = -1L

    override fun dispose() {
    }
}