package com.edd.jelly.behaviour.sound

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.EntitySystem
import com.edd.jelly.core.events.Messaging
import com.edd.jelly.core.resources.ResourceManager
import com.google.inject.Inject

class SoundSystem @Inject constructor(
        private val resourceManager: ResourceManager,
        private val messaging: Messaging
): EntitySystem() {

    override fun addedToEngine(engine: Engine?) {
        messaging.listen<PlaySoundEvent> {
            // todo resourceManager.getSound()
        }

        messaging.listen<PlayMusicEvent> {
            // todo resourceManager.getMusic()
        }
    }
}