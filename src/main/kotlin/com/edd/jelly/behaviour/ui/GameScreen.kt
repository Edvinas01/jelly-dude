package com.edd.jelly.behaviour.ui

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.ui.Value.*
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.edd.jelly.behaviour.level.LoadNewLevelEvent
import com.edd.jelly.behaviour.level.RestartLevelEvent
import com.edd.jelly.core.GuiCamera
import com.edd.jelly.behaviour.pause.PauseEvent
import com.edd.jelly.core.configuration.Configurations.Companion.MENU_LEVEL_NAME
import com.edd.jelly.core.events.Messaging
import com.edd.jelly.core.resources.ResourceManager
import com.google.inject.Inject

class GameScreen @Inject constructor(
        private val resourceManager: ResourceManager,
        private val messaging: Messaging,
        @GuiCamera
        camera: OrthographicCamera,
        batch: SpriteBatch
) : StagedScreen(camera, batch) {

    private val skin = resourceManager.skin

    // Pause window widgets.
    private val pauseWindow = Window("Paused", skin, "jelly")
    private val restartButton = TextButton("Restart", skin)
    private val mainMenuButton = TextButton("Main menu", skin)
    private val exitButton = TextButton("Exit game", skin)

    private val menu = createMenuTable()

    init {
        stage.addListener(object : InputListener() {
            override fun keyDown(event: InputEvent, keycode: Int): Boolean {
                if (Input.Keys.ESCAPE == keycode) {
                    val showingMenu = stage.actors.contains(menu)

                    if (showingMenu) {
                        menu.remove()
                    } else {
                        stage.addActor(menu)
                    }

                    pause(!showingMenu)
                    return true
                }
                return false
            }
        })
    }

    /**
     * Send event to pause the game engine.
     */
    private fun pause(pause: Boolean = true) {
        messaging.send(PauseEvent(pause))
    }

    /**
     * Create pause menu table.
     */
    private fun createMenuTable(): Table {
        val root = Table().apply {
            setFillParent(true)
        }

        pauseWindow.apply {
            isMovable = false
            isModal = false
        }

        root.add(pauseWindow)
                .prefWidth(percentWidth(0.20f, root))

        // Button to restart the level.
        val restartCell = pauseWindow.add(restartButton.apply {
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
        val menuCell = pauseWindow.add(mainMenuButton.apply {
            addListener(object : ClickListener() {
                override fun clicked(event: InputEvent, x: Float, y: Float) {
                    messaging.send(LoadNewLevelEvent(MENU_LEVEL_NAME, true))
                    messaging.send(LoadMainMenuScreenEvent)
                }
            })
        })

        menuCell.fillX()
                .row()

        // Button to exit the game.
        val exitCell = pauseWindow.add(exitButton.apply {
            addListener(object : ClickListener() {
                override fun clicked(event: InputEvent, x: Float, y: Float) {
                    Gdx.app.exit()
                }
            })
        })

        exitCell.fillX()
                .expand()

        return root
    }

    override fun updateLanguage() {
        pauseWindow.titleLabel.setText(resourceManager.getMessage("pauseWindowTitle"))
        restartButton.setText(resourceManager.getMessage("pauseRestartButton"))
        mainMenuButton.setText(resourceManager.getMessage("pauseMenuButton"))
        exitButton.setText(resourceManager.getMessage("pauseExitButton"))
    }
}