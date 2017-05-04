package com.edd.jelly.behaviour.ui

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.gdx.InputMultiplexer
import com.edd.jelly.behaviour.ui.screen.GameScreen
import com.edd.jelly.behaviour.ui.screen.MainMenuScreen
import com.edd.jelly.behaviour.ui.screen.StagedScreen
import com.edd.jelly.core.JellyGame
import com.edd.jelly.core.configuration.ConfigChangedEvent
import com.edd.jelly.core.events.Messaging
import com.edd.jelly.core.resources.ResourceManager
import com.edd.jelly.util.GameException
import com.google.inject.Inject
import com.google.inject.Injector
import com.google.inject.Singleton

@Singleton
class UISystem @Inject constructor(
        private val inputMultiplexer: InputMultiplexer,
        private val resources: ResourceManager,
        private val messaging: Messaging,
        private val injector: Injector,
        private val game: JellyGame
) : EntitySystem() {

    override fun addedToEngine(engine: Engine) {
        initListeners()

        // Initially always load main menu.
        setRootScreen(injector.getInstance(MainMenuScreen::class.java))
    }

    /**
     * Replace current root screen with a new one.
     */
    private fun setRootScreen(screen: StagedScreen) {
        val current = game.screen
        if (current != null) {
            if (current is StagedScreen) {
                inputMultiplexer.removeProcessor(current.stage)
            } else {
                throw GameException("Screen must of type ${StagedScreen::class}")
            }

            current.dispose()
        }
        screen.updateLanguage(resources.language)

        inputMultiplexer.addProcessor(0, screen.stage)
        game.screen = screen
    }

    /**
     * Initialize UI listeners.
     */
    private fun initListeners() {
        messaging.listen<ConfigChangedEvent> {
            val screen = game.screen
            if (screen != null && screen is StagedScreen) {
                screen.updateLanguage(resources.language)
            }
        }

        messaging.listen<LoadGameScreenEvent> {
            setRootScreen(injector.getInstance(GameScreen::class.java))
        }

        messaging.listen<LoadMainMenuScreenEvent> {
            setRootScreen(injector.getInstance(MainMenuScreen::class.java))
        }
    }
}