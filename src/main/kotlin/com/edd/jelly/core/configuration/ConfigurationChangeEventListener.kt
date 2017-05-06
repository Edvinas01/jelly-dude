package com.edd.jelly.core.configuration

import com.badlogic.gdx.Gdx
import com.edd.jelly.core.events.Listener
import com.edd.jelly.behaviour.common.event.ConfigChangedEvent
import com.google.inject.Inject

class ConfigurationChangeEventListener @Inject constructor() : Listener<ConfigChangedEvent> {

    override fun listen(event: ConfigChangedEvent) {
        val screen = event.config.video.screen

        if (screen.fullscreen != Gdx.graphics.isFullscreen) {
            if (screen.fullscreen) {
                Gdx.graphics.setFullscreenMode(Gdx.graphics.displayMode)
            } else {
                Gdx.graphics.setWindowedMode(screen.width, screen.height)
            }
        }
    }
}