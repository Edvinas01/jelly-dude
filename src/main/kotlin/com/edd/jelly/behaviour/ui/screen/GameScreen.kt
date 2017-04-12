package com.edd.jelly.behaviour.ui.screen

import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.Value.percentWidth
import com.edd.jelly.behaviour.pause.PauseEvent
import com.edd.jelly.behaviour.ui.screen.windows.PauseWindow
import com.edd.jelly.core.GuiCamera
import com.edd.jelly.core.events.Messaging
import com.edd.jelly.core.resources.Language
import com.edd.jelly.core.resources.ResourceManager
import com.google.inject.Inject

class GameScreen @Inject constructor(
        resources: ResourceManager,
        messaging: Messaging,
        @GuiCamera
        camera: OrthographicCamera,
        batch: SpriteBatch
) : StagedScreen(camera, batch) {

    private val pauseWindow = PauseWindow(messaging, resources.skin)

    init {
        val mainTable = Table().apply {
            setFillParent(true)
            add(pauseWindow).prefWidth(percentWidth(0.20f, this))
        }

        stage.addListener(object : InputListener() {
            override fun keyDown(event: InputEvent, keycode: Int): Boolean {
                if (Input.Keys.ESCAPE == keycode) {
                    val showingMenu = stage.actors.contains(mainTable)

                    if (showingMenu) {
                        mainTable.remove()
                    } else {
                        stage.addActor(mainTable)
                    }

                    messaging.send(PauseEvent(!showingMenu))
                    return true
                }
                return false
            }
        })
    }

    override fun updateLanguage(lang: Language) {
        pauseWindow.updateLanguage(lang)
    }
}