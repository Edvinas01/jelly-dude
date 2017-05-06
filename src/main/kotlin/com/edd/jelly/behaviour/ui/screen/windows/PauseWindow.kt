package com.edd.jelly.behaviour.ui.screen.windows

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.ui.Window
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.edd.jelly.behaviour.common.event.LoadNewLevelEvent
import com.edd.jelly.behaviour.common.event.RestartLevelEvent
import com.edd.jelly.behaviour.ui.screen.LanguageAware
import com.edd.jelly.behaviour.common.event.LoadGameScreenEvent
import com.edd.jelly.behaviour.common.event.LoadMainMenuScreenEvent
import com.edd.jelly.core.configuration.Configurations
import com.edd.jelly.core.events.Messaging
import com.edd.jelly.core.resources.Language

class PauseWindow constructor(
        messaging: Messaging,
        skin: Skin
) : Window("Paused", skin, "jelly"), LanguageAware {

    private val restartButton = TextButton("Restart", skin)
    private val mainMenuButton = TextButton("Main menu", skin)
    private val exitButton = TextButton("Exit game", skin)

    init {
        isMovable = false
        isModal = false

        // Button to restart the level.
        val restartCell = add(restartButton.apply {
            addListener(object : ClickListener() {
                override fun clicked(event: InputEvent, x: Float, y: Float) {
                    messaging.send(RestartLevelEvent)
                    messaging.send(LoadGameScreenEvent)
                }
            })
        })

        restartCell
                .fillX()
                .row()

        // Button to open main menu.
        val menuCell = add(mainMenuButton.apply {
            addListener(object : ClickListener() {
                override fun clicked(event: InputEvent, x: Float, y: Float) {
                    messaging.send(LoadNewLevelEvent(Configurations.Companion.MENU_LEVEL_NAME, true))
                    messaging.send(LoadMainMenuScreenEvent)
                }
            })
        })

        menuCell.fillX()
                .row()

        // Button to exit the game.
        val exitCell = add(exitButton.apply {
            addListener(object : ClickListener() {
                override fun clicked(event: InputEvent, x: Float, y: Float) {
                    Gdx.app.exit()
                }
            })
        })

        exitCell.fillX()
                .expand()
    }

    override fun updateLanguage(lang: Language) {
        titleLabel.setText(lang["pauseWindowTitle"])
        restartButton.setText(lang["pauseRestartButton"])
        mainMenuButton.setText(lang["pauseMenuButton"])
        exitButton.setText(lang["exitButton"])
    }
}