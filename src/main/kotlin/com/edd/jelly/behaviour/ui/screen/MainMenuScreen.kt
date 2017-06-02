package com.edd.jelly.behaviour.ui.screen

import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.ui.Value.percentHeight
import com.badlogic.gdx.scenes.scene2d.ui.Value.percentWidth
import com.edd.jelly.behaviour.ui.screen.windows.LevelsWindow
import com.edd.jelly.behaviour.ui.screen.windows.MenuWindow
import com.edd.jelly.behaviour.ui.screen.windows.OptionsWindow
import com.edd.jelly.game.GuiCamera
import com.edd.jelly.core.configuration.Configurations
import com.edd.jelly.core.events.Messaging
import com.edd.jelly.core.resources.Language
import com.edd.jelly.core.resources.ResourceManager
import com.edd.jelly.core.tiled.JellyMapLoader
import com.google.inject.Inject

class MainMenuScreen @Inject constructor(
        val resources: ResourceManager,
        configurations: Configurations,
        jellyMapLoader: JellyMapLoader,
        messaging: Messaging,
        @GuiCamera
        camera: OrthographicCamera,
        batch: SpriteBatch
) : StagedScreen(camera, batch) {

    private val skin = resources.skin

    private val gameTitle = Label("Jelly Dude", skin, "title")
    private val levelsWindow = LevelsWindow(
            skin,
            jellyMapLoader,
            messaging,
            resources.language.handle.internalName
    )

    private val optionsWindow = OptionsWindow(
            configurations,
            skin,
            resources.languages,
            resources.language
    )

    private val menuWindow = MenuWindow(
            skin,
            this::setActive,
            levelsWindow,
            optionsWindow
    )

    // Active control.
    private val active: Cell<Actor>

    init {
        val mainTable = Table().apply {
            top().left().setFillParent(true)

            // Main header.
            add(gameTitle)
                    .height(percentHeight(0.1f, this))
                    .colspan(2)
                    .fill()
                    .pad(8f)
                    .top()
                    .left()
                    .row()

            // Main controls.
            add(menuWindow)
                    .width(percentWidth(0.25f, this))
                    .expand()
                    .fill()
        }

        // Cell for dropping in control menus.
        this.active = mainTable
                .add()
                .width(percentWidth(0.75f, mainTable))
                .expand()
                .fill()

        this.stage.addActor(mainTable)
    }

    /**
     * Show error pop-up window with a text message.
     */
    fun showErrorPopUp(message: String) {
        val lang = resources.language
        Dialog(lang["errorTitle"], skin, "jelly").apply {
            key(Input.Keys.ENTER, true)

            text(message)
            button(lang["closeButton"])
        }.show(stage)
    }

    /**
     * Set active control table.
     */
    private fun setActive(actor: Table) {
        val activeActor = active.actor

        if (activeActor != null && activeActor == actor) {
            active.setActor<Table>(null)
        } else {
            active.setActor<Table>(actor)
        }
    }

    override fun updateLanguage(lang: Language) {
        gameTitle.setText(lang["gameTitle"])
        levelsWindow.updateLanguage(lang)
        optionsWindow.updateLanguage(lang)
        menuWindow.updateLanguage(lang)
    }
}