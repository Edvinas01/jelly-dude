package com.edd.jelly.behaviour.ui

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.ui.Value.*
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.edd.jelly.behaviour.level.LoadNewLevelEvent
import com.edd.jelly.core.GuiCamera
import com.edd.jelly.core.events.Messaging
import com.edd.jelly.core.resources.ResourceManager
import com.edd.jelly.core.resources.get
import com.edd.jelly.core.tiled.JellyMapLoader
import com.google.inject.Inject

// Some notes to self:
// The fill() method causes a widget to be sized to the cell.
// Use expand() to make the logical table take up the entire size of the table widget.
// Red lines show the cell bounds and the green lines show the widget bounds.
// The outer blue rectangle shows the size of the table widget. The inner blue rectangle shows the size of the logical
// table, which is aligned to center by default
class MainMenuScreen @Inject constructor(
        private val jellyMapLoader: JellyMapLoader,
        private val messaging: Messaging,
        resources: ResourceManager,
        @GuiCamera
        camera: OrthographicCamera,
        batch: SpriteBatch
) : StagedScreen(camera, batch) {

    private companion object {
        const val TOGGLE_STYLE = "toggle-simple"
        const val PAD = 8f
    }

    private val options = options(resources.skin)
    private val levels = levels(resources.skin, resources.mainAtlas)
    private val active: Cell<Actor>

    init {
        val (root, active) = mainTable(resources.skin)

        this.stage.addActor(root)
        this.active = active
    }

    /**
     * Create options controls.
     */
    private fun options(skin: Skin): Table {
        return Window("Options", skin).apply {
            isMovable = false
            isModal = false
        }
    }

    /**
     * Create level selection controls.
     */
    private fun levels(skin: Skin, textureAtlas: TextureAtlas): Table {
        val levels = Window("Levels", skin).apply {
            isMovable = false
            isModal = false
        }

        val levelContainer = Table()
                .top()
                .left()

        val elementPad = percentWidth(0.01f, levelContainer)
        val elementWidth = percentWidth(0.23f, levelContainer)

        for ((index, meta) in jellyMapLoader.metadata.withIndex()) {
            if (index > 0 && index % 4 == 0) {
                levelContainer.row()
            }

            // Details about the level.
            val levelDetails = Table()

            // Level image.
            levelDetails
                    .add(Image(textureAtlas["the_borker"]))
                    .fill()
                    .row()

            // Launch level button.
            val playCell = levelDetails.add(TextButton(meta.name, skin).apply {
                label.setWrap(true)
                label.setEllipsis(true)

                addListener(object : ClickListener() {
                    override fun clicked(event: InputEvent, x: Float, y: Float) {
                        messaging.send(LoadNewLevelEvent(meta.name))
                    }
                })
            })

            playCell.expand()
                    .fill()

            levelContainer
                    .add(levelDetails)
                    .padBottom(PAD)
                    .padLeft(elementPad)
                    .padRight(elementPad)
                    .width(elementWidth)
                    .expand()
        }

        // Scroll for levels.
        val scroll = ScrollPane(levelContainer, skin).apply {
            setFadeScrollBars(false)
        }

        levels.add(scroll)
                .grow()

        return levels
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

    /**
     * Initialize root table with controls.
     */
    private fun mainTable(skin: Skin): Pair<Table, Cell<Actor>> {
        val rootTable = Table().apply {
            top().left().setFillParent(true)
        }

        val controls = Window("Menu", skin).apply {
            isMovable = false
            isModal = false
        }

        // Main header.
        rootTable
                .add(Label("Jelly Dude", skin, "title"))
                .height(percentHeight(0.1f, rootTable))
                .colspan(2)
                .fill()
                .pad(PAD)
                .top()
                .left()
                .row()

        // Main controls.
        rootTable
                .add(controls)
                .width(percentWidth(0.25f, rootTable))
                .expand()
                .fill()

        // Cell for dropping in control menus.
        val selected = rootTable
                .add()
                .width(percentWidth(0.75f, rootTable))
                .expand()
                .fill()

        // Common padding for control button cells.
        val buttonPad = percentHeight(0.05f, controls)

        // Button for showing level selection window.
        val playButton = TextButton("Play", skin, TOGGLE_STYLE).apply {
            addListener(object : ClickListener() {
                override fun clicked(event: InputEvent, x: Float, y: Float) {
                    setActive(levels)
                }
            })
        }

        controls.add(playButton)
                .padBottom(buttonPad)
                .fillX()
                .row()

        // Button for showing options window.
        val optionsButton = TextButton("Options", skin, TOGGLE_STYLE).apply {
            addListener(object : ClickListener() {
                override fun clicked(event: InputEvent, x: Float, y: Float) {
                    setActive(options)
                }
            })
        }

        controls.add(optionsButton)
                .padBottom(buttonPad)
                .fillX()
                .row()

        // Exit game button.
        val exitCell = controls.add(TextButton("Exit", skin).apply {
            addListener(object : ClickListener() {
                override fun clicked(event: InputEvent, x: Float, y: Float) {
                    Gdx.app.exit()
                }
            })
        })

        // Make sure only one control button is checked at a time.
        ButtonGroup(playButton, optionsButton).apply {
            setMaxCheckCount(1)
            setMinCheckCount(0)
            setUncheckLast(true)
        }

        exitCell.fillX()
                .expand()
                .bottom()
                .left()

        return Pair(rootTable, selected)
    }
}