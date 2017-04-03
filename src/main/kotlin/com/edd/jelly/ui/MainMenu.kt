package com.edd.jelly.ui

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.ui.Value.percentHeight
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.viewport.FitViewport
import com.edd.jelly.core.GuiCamera
import com.edd.jelly.core.resources.ResourceManager
import com.google.inject.Inject

/**
 * Note that main menu is created on request, it is not a singleton!
 */
class MainMenu @Inject constructor(
        resources: ResourceManager,
        batch: SpriteBatch,
        @GuiCamera
        private val camera: OrthographicCamera
) : ScreenAdapter() {

    val stage = Stage(FitViewport(camera.viewportWidth, camera.viewportHeight), batch)
    private val skin = resources.skin

    private val options = optionsTable()
    private val levels = levelsTable()

    init {
        stage.addActor(rootTable())
    }

    /**
     * Create options controls.
     */
    private fun optionsTable(): Table {
        val options = Table()
        options.add(Label("Options", skin))
        return options
    }

    /**
     * Create level selection controls.
     */
    private fun levelsTable(): Table {
        val options = Table()
        options.add(Label("Levels", skin))
        return options
    }

    /**
     * Initialize root table with controls.
     */
    private fun rootTable(): Table {
        val rootTable = Table().apply {
            top().left().setFillParent(true)
        }

        // Buttons and such.
        val controls = Table()

        // Add controls to the root table.
        rootTable.add(controls)
                .width(camera.viewportWidth / 4)
                .expandY()
                .pad(16f)
                .top()
                .left()

        // Placeholder for play or options menu.
        val dynamicCell = rootTable.add().expand()

        // Main menu label.
        val menuLabel = Label("Main Menu", skin).apply {
            setFontScale(2f)
        }

        controls.add(menuLabel)
                .prefHeight(percentHeight(25f))
                .expand()
                .padBottom(32f)
                .row()

        // Button to open levels menu.
        val levelsButton = TextButton("Play", skin)
        levelsButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent, x: Float, y: Float) {
                if (levelsButton.isChecked) {
                    dynamicCell.setActor<Table>(levels)
                } else {
                    dynamicCell.setActor<Actor>(null)
                }
            }
        })

        controls.add(levelsButton)
                .prefHeight(percentHeight(25f))
                .fill()
                .pad(16f)
                .row()

        // Options menu button.
        val optionsButton = TextButton("Options", skin)
        optionsButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent, x: Float, y: Float) {
                if (optionsButton.isChecked) {
                    dynamicCell.setActor<Table>(options)
                } else {
                    dynamicCell.setActor<Actor>(null)
                }
            }
        })

        controls.add(optionsButton)
                .prefHeight(percentHeight(25f))
                .fill()
                .pad(16f)
                .row()

        // Exit button.
        val exitButton = TextButton("Exit", skin)
        exitButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent, x: Float, y: Float) {
                Gdx.app.exit()
            }
        })

        controls.add(exitButton)
                .prefHeight(percentHeight(25f))
                .fill()
                .pad(16f)

        return rootTable.debugAll()
    }

    override fun render(delta: Float) {
        stage.act(delta)
        stage.draw()
    }

    override fun dispose() {
        stage.dispose()
    }

    override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)
    }
}