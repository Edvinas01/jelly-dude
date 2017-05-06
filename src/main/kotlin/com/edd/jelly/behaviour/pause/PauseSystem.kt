package com.edd.jelly.behaviour.pause

import com.badlogic.ashley.core.EntitySystem
import com.edd.jelly.behaviour.common.event.LoadNewLevelEvent
import com.edd.jelly.behaviour.common.event.RestartLevelEvent
import com.edd.jelly.core.events.Messaging
import com.edd.jelly.behaviour.common.event.PauseEvent
import com.google.inject.Inject
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class PauseSystem @Inject constructor(
        messaging: Messaging
) : EntitySystem() {

    private companion object {
        val LOG: Logger = LogManager.getLogger(PauseSystem::class.java)
    }

    init {
        messaging.listen<PauseEvent> {
            pause(it.pause)
        }

        messaging.listen<LoadNewLevelEvent> {
            pause(false)
        }

        messaging.listen<RestartLevelEvent> {
            pause(false)
        }
    }

    private fun pause(pause: Boolean = true) {
        if (LOG.isDebugEnabled) {
            LOG.debug("{} systems", if (pause) "Pausing" else "Un-pausing")
        }

        engine.systems.forEach {
            if (it is PausingSystem) {
                it.setProcessing(!pause)
                it.paused(pause)
            }
        }
    }
}