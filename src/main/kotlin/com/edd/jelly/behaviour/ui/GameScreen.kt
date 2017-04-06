package com.edd.jelly.behaviour.ui

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.ui.Value.*
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.Align
import com.edd.jelly.behaviour.level.LoadNewLevelEvent
import com.edd.jelly.behaviour.level.RestartLevelEvent
import com.edd.jelly.core.GuiCamera
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

    private companion object {
        const val BUTTON_WIDTH = 0.2f
        const val PADDING = 0.02f
    }

    private val skin = resourceManager.skin
    private val menu = createMenuTable()

    init {
        stage.addListener(object : InputListener() {
            override fun keyDown(event: InputEvent, keycode: Int): Boolean {
                if (Input.Keys.ESCAPE == keycode) {
                    if (stage.actors.contains(menu)) {
                        menu.remove()
                    } else {
                        stage.addActor(menu)
                    }
                    return true
                }
                return false
            }
        })
    }

    /**
     * Create pause menu table.
     */
    private fun createMenuTable(): Table {
        val label = Label("Paused", skin).apply {
            setAlignment(Align.center)
        }

        val restartButton = TextButton("Restart", skin).apply {
            addListener(object : ClickListener() {
                override fun clicked(event: InputEvent, x: Float, y: Float) {
                    messaging.send(RestartLevelEvent)
                }
            })
        }

        val mainMenuButton = TextButton("Main menu", skin).apply {
            addListener(object : ClickListener() {
                override fun clicked(event: InputEvent, x: Float, y: Float) {
                    messaging.send(LoadNewLevelEvent(MENU_LEVEL_NAME, true))
                    messaging.send(LoadMainMenuEvent)
                }
            })
        }

        val exitButton = TextButton("Exit game", skin).apply {
            addListener(object : ClickListener() {
                override fun clicked(event: InputEvent, x: Float, y: Float) {
                    Gdx.app.exit()
                }
            })
        }

        val root = Table().apply {
            setFillParent(true)
        }

        val padding = percentHeight(PADDING, root)

        root.add(Table().apply {
            add(label)
                    .padBottom(padding)
                    .width(percentWidth(BUTTON_WIDTH, root))
                    .row()

            add(restartButton)
                    .padBottom(padding)
                    .fillX()
                    .row()

            add(mainMenuButton)
                    .padBottom(padding)
                    .fillX()
                    .row()

            add(exitButton)
                    .fillX()
                    .row()
        })
        return root
    }
}