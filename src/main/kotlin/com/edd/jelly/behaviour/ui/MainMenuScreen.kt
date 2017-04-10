package com.edd.jelly.behaviour.ui

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.ui.Value.*
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.Array
import com.edd.jelly.behaviour.level.LoadNewLevelEvent
import com.edd.jelly.core.GuiCamera
import com.edd.jelly.core.configuration.Configurations
import com.edd.jelly.core.events.Messaging
import com.edd.jelly.core.resources.Language
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
        private val configurations: Configurations,
        private val jellyMapLoader: JellyMapLoader,
        private val messaging: Messaging,
        private val resources: ResourceManager,
        @GuiCamera
        camera: OrthographicCamera,
        batch: SpriteBatch
) : StagedScreen(camera, batch) {

    private val skin = resources.skin

    // Main widgets.
    private val gameTitle = Label("Jelly Dude", skin, "title")

    // Control widgets.
    private val menuWindow = Window("Menu", skin, "jelly")
    private val menuPlayButton = TextButton("Play", skin, "toggle-simple")
    private val menuOptionsButton = TextButton("Options", skin, "toggle-simple")
    private val menuExitButton = TextButton("Exit", skin)

    // Level selection widgets.
    private val levelsWindow = Window("Levels", skin, "jelly")

    // Option widgets.
    private val optionsWindow = Window("Options", skin, "jelly")
    private val optionsLanguageLabel = Label("Language", skin)
    private val languageSelect = SelectBox<Language>(skin)

    // Active control.
    private val active: Cell<Actor>

    init {
        val (root, active) = mainTable()

        this.stage.addActor(root)
        this.active = active

        populateLevels(skin, resources.mainAtlas)
        populateOptions()
    }

    /**
     * Populate level selection controls.
     */
    private fun populateLevels(skin: Skin, textureAtlas: TextureAtlas) {
        levelsWindow.apply {
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
                        messaging.send(LoadGameScreenEvent)
                    }
                })
            })

            playCell.expand()
                    .fill()

            levelContainer
                    .add(levelDetails)
                    .padBottom(8f)
                    .padLeft(elementPad)
                    .padRight(elementPad)
                    .width(elementWidth)
                    .expand()
        }

        // Scroll for levels.
        val scroll = ScrollPane(levelContainer, skin)

        levelsWindow
                .add(scroll)
                .grow()
    }

    /**
     * Populate options controls.
     */
    private fun populateOptions() {
        optionsWindow.apply {
            isMovable = false
            isModal = false
        }

        // Language form group.
        val languageGroup = Table()

        val langArray = Array<Language>()
        resources.getLanguages().forEach {
            langArray.add(it)
        }

        val game = configurations.config.game

        languageSelect.items = langArray
        languageSelect.selected = langArray.find {
            it.internalName == game.language
        }

        languageSelect.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor?) {
                if (game.language != languageSelect.selected.internalName) {
                    game.language = languageSelect.selected.internalName
                    configurations.save()
                }
            }
        })

        languageGroup
                .add(optionsLanguageLabel)
                .growX()
                .row()

        languageGroup
                .add(languageSelect)
                .growX()

        // Add language group to options.
        optionsWindow.add(languageGroup)
                .width(percentWidth(0.25f, optionsWindow))
                .top().left()
                .expand()
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
    private fun mainTable(): Pair<Table, Cell<Actor>> {
        val rootTable = Table().apply {
            top().left().setFillParent(true)
        }

        val controls = menuWindow.apply {
            isMovable = false
            isModal = false
        }

        // Main header.
        rootTable
                .add(gameTitle)
                .height(percentHeight(0.1f, rootTable))
                .colspan(2)
                .fill()
                .pad(8f)
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
        val playButton = menuPlayButton.apply {
            addListener(object : ClickListener() {
                override fun clicked(event: InputEvent, x: Float, y: Float) {
                    setActive(levelsWindow)
                }
            })
        }

        controls.add(playButton)
                .padBottom(buttonPad)
                .fillX()
                .row()

        // Button for showing options window.
        val optionsButton = menuOptionsButton.apply {
            addListener(object : ClickListener() {
                override fun clicked(event: InputEvent, x: Float, y: Float) {
                    setActive(optionsWindow)
                }
            })
        }

        controls.add(optionsButton)
                .padBottom(buttonPad)
                .fillX()
                .row()

        // Exit game button.
        val exitCell = controls.add(menuExitButton.apply {
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

    override fun updateLanguage() {
        gameTitle.setText(resources.getMessage("gameTitle"))

        menuWindow.titleLabel.setText(resources.getMessage("menuWindowTitle"))

        menuPlayButton.setText(resources.getMessage("menuPlayButton"))
        menuOptionsButton.setText(resources.getMessage("menuOptionsButton"))
        menuExitButton.setText(resources.getMessage("menuExitButton"))

        levelsWindow.titleLabel.setText(resources.getMessage("levelsWindowTitle"))

        optionsWindow.titleLabel.setText(resources.getMessage("optionsWindowTitle"))
        optionsLanguageLabel.setText(resources.getMessage("optionsLanguageLabel"))
    }
}