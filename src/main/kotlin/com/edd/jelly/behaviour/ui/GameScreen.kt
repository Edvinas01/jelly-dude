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
        private val messaging: Messaging,
        resourceManager: ResourceManager,
        @GuiCamera
        camera: OrthographicCamera,
        batch: SpriteBatch
) : StagedScreen(camera, batch) {

    private val menu = createMenuTable(resourceManager.skin)

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
    private fun createMenuTable(skin: Skin): Table {
        val root = Table().apply {
            setFillParent(true)
        }

        val menu = Window("Paused", skin).apply {
            isMovable = false
            isModal = false
        }

        root.add(menu)
                .width(percentWidth(0.20f, root))

        // Button to restart the level.
        val restartCell = menu.add(TextButton("Restart", skin).apply {
            addListener(object : ClickListener() {
                override fun clicked(event: InputEvent, x: Float, y: Float) {
                    messaging.send(RestartLevelEvent)
                }
            })
        })

        restartCell
                .fillX()
                .row()

        // Button to open main menu.
        val menuCell = menu.add(TextButton("Main menu", skin).apply {
            addListener(object : ClickListener() {
                override fun clicked(event: InputEvent, x: Float, y: Float) {
                    messaging.send(LoadNewLevelEvent(MENU_LEVEL_NAME, true))
                    messaging.send(LoadMainMenuEvent)
                }
            })
        })

        menuCell.fillX()
                .row()

        // Button to exit the game.
        val exitCell = menu.add(TextButton("Exit game", skin).apply {
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
}