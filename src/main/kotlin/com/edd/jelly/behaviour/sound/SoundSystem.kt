package com.edd.jelly.behaviour.sound

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.ashley.core.Family
import com.badlogic.gdx.math.MathUtils
import com.edd.jelly.behaviour.common.event.ConfigChangedEvent
import com.edd.jelly.behaviour.common.event.PlayMusicEvent
import com.edd.jelly.behaviour.common.event.PlaySoundEvent
import com.edd.jelly.core.configuration.Configurations
import com.edd.jelly.core.events.Messaging
import com.edd.jelly.core.resources.ResourceManager
import com.edd.jelly.util.EntityListenerAdapter
import com.google.inject.Inject

class SoundSystem @Inject constructor(
        private val resourceManager: ResourceManager,
        private val configurations: Configurations,
        private val messaging: Messaging
) : EntitySystem() {

    override fun addedToEngine(engine: Engine) {
        val musicFamily = Family.all(PlayingMusic::class.java).get()
        val soundFamily = Family.all(LoopingSound::class.java).get()

        initVolumeListeners(musicFamily, soundFamily)
        initMusicListeners(musicFamily)
        initSoundListeners(soundFamily)
    }

    /**
     * Initialize listeners for sound volume changes.
     */
    private fun initVolumeListeners(musicFamily: Family, soundFamily: Family) {
        messaging.listen<ConfigChangedEvent> { (c) ->
            engine.getEntitiesFor(musicFamily).forEach {
                PlayingMusic[it].music.volume = c.game.musicVolume
            }

            engine.getEntitiesFor(soundFamily).forEach {
                val looping = LoopingSound[it]
                looping.sound.setVolume(looping.id, c.game.soundVolume)
            }
        }
    }

    /**
     * Initialize listeners for playing, stopping and looping sounds.
     */
    private fun initSoundListeners(soundFamily: Family) {
        messaging.listen<PlaySoundEvent> { (name, low, high, loop, volumeMultiplier) ->
            val volume = configurations.config.game.soundVolume * volumeMultiplier
            val sound = resourceManager.getSound(name)

            if (low < 1f || high < 1f) {
                if (loop) {
                    val id = sound.loop(volume, MathUtils.random(low, high), 0.5f)
                    engine.addEntity(Entity().add(LoopingSound(id, sound)))
                } else {
                    sound.play(volume, MathUtils.random(low, high), 0.5f)
                }

            } else {
                if (loop) {
                    val id = sound.loop(volume)
                    engine.addEntity(Entity().add(LoopingSound(id, sound)))
                } else {
                    sound.play(volume)
                }
            }
        }

        engine.addEntityListener(soundFamily, object : EntityListenerAdapter() {
            override fun entityRemoved(entity: Entity) {
                LoopingSound[entity].sound.stop()
            }
        })
    }

    /**
     * Initialize listeners for playing and stopping music.
     */
    private fun initMusicListeners(musicFamily: Family) {
        messaging.listen<PlayMusicEvent> { (name, loop) ->
            val music = resourceManager.getMusic(name)
            music.isLooping = loop
            music.volume = configurations.config.game.soundVolume
            music.play()
            engine.addEntity(Entity().add(PlayingMusic(music)))
        }

        engine.addEntityListener(musicFamily, object : EntityListenerAdapter() {
            override fun entityRemoved(entity: Entity) {
                PlayingMusic[entity].music.stop()
            }
        })
    }
}