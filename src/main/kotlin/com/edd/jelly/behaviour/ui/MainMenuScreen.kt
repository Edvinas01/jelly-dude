package com.edd.jelly.behaviour.ui

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.ui.Value.percentHeight
import com.badlogic.gdx.scenes.scene2d.ui.Value.percentWidth
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.Align
import com.edd.jelly.behaviour.level.LoadNewLevelEvent
import com.edd.jelly.core.GuiCamera
import com.edd.jelly.core.configuration.Configurations
import com.edd.jelly.core.events.Messaging
import com.edd.jelly.core.resources.ResourceManager
import com.edd.jelly.core.tiled.JellyMapLoader
import com.google.inject.Inject

/**
 * Note that main menu is created on request, it is not a singleton!
 */

// The fill method causes a widget to be sized to the cell
// Expand to make the logical table take up the entire size of the table widget
class MainMenuScreen @Inject constructor(
        private val jellyMapLoader: JellyMapLoader,
        private val messaging: Messaging,
        configurations: Configurations,
        resources: ResourceManager,
        @GuiCamera
        camera: OrthographicCamera,
        batch: SpriteBatch
) : StagedScreen(camera, batch) {

    private companion object {
        val BUTTON_PADDING = 30f
        val TABLE_PAD = 16f
        val BUTTON_HEIGHT = percentHeight(10f)!!
    }

    private val config = configurations.config
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
                .top()
                .left()
                .pad(TABLE_PAD)

        options.add(Label("Options", skin).apply {
            setFontScale(1.5f)
        }).left()

        return options.debugAll()
    }

    /**
     * Create level selection controls.
     */
    private fun levelsTable(): Table {
        val levels = Table()
                .top()
                .left()
                .pad(TABLE_PAD)

        val scrollContent = Table()
                .top()
                .left()

        for ((index, meta) in jellyMapLoader.metadata.withIndex()) {
            if (index > 0 && index % 4 == 0) {
                scrollContent.row()
            }

            val levelInfo = Table()
            levelInfo.add(Label(meta.name, skin).apply {
                setAlignment(Align.center)
            }).prefHeight(170f)
                    .expandX()
                    .fillX()
                    .row()

            val play = TextButton("Play ${meta.name}", skin)
            play.addListener(object : ClickListener() {
                override fun clicked(event: InputEvent, x: Float, y: Float) {
                    messaging.send(LoadNewLevelEvent(meta.name))
                }
            })
            levelInfo.add(play).fillX()

            scrollContent.add(levelInfo)
                    .width(percentWidth(0.23f, scrollContent))
                    .pad(percentWidth(0.01f, scrollContent))
                    .fill()
        }

        levels.add(Label("Levels", skin).apply {
            setFontScale(1.5f)
        }).left()

        val scroll = ScrollPane(scrollContent, skin)
        scroll.setFadeScrollBars(false)

        levels.row()
        levels.add(scroll).expand().fill()

        return levels.debugAll()
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
                .top()
                .left()

        // Add controls to the root table.
        rootTable.add(controls)
                .width(camera.viewportWidth / 4)
                .expandY()
                .pad(16f)

        // Placeholder for play or options menu.
        val dynamicCell = rootTable
                .add()
                .expand()
                .fill()

        // Main menu label.
        val menuLabel = Label("Main Menu", skin).apply {
            setFontScale(2f)
        }

        // Button to open levels menu.
        val levelsButton = TextButton("Play", skin).apply {
            addListener(object : ClickListener() {
                override fun clicked(event: InputEvent, x: Float, y: Float) {
                    dynamicCell.setActor<Table>(levels)
                }
            })
        }

        // Options menu button.
        val optionsButton = TextButton("Options", skin).apply {
            addListener(object : ClickListener() {
                override fun clicked(event: InputEvent, x: Float, y: Float) {
                    dynamicCell.setActor<Table>(options)
                }
            })
        }

        // Exit button.
        val exitButton = TextButton("Exit", skin).apply {
            addListener(object : ClickListener() {
                override fun clicked(event: InputEvent, x: Float, y: Float) {
                    Gdx.app.exit()
                }
            })
        }

        controls.add(menuLabel)
                .prefHeight(percentHeight(25f)) // todo precentages wrong
                .expand()
                .padBottom(32f)
                .row()

        controls.add(levelsButton)
                .prefHeight(BUTTON_HEIGHT)
                .fill()
                .pad(BUTTON_PADDING)
                .row()

        controls.add(optionsButton)
                .prefHeight(BUTTON_HEIGHT)
                .fill()
                .pad(BUTTON_PADDING)
                .row()

        controls.add(exitButton)
                .prefHeight(BUTTON_HEIGHT)
                .fill()
                .pad(BUTTON_PADDING)

        if (config.game.uiDebug) {
            return rootTable.debugAll()
        } else {
            return rootTable
        }
    }
}