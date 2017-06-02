package com.edd.jelly.behaviour.ui.screen.windows

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.edd.jelly.behaviour.ui.screen.LanguageAware
import com.edd.jelly.core.resources.Language

class MenuWindow constructor(
        skin: Skin,
        setActive: (selected: Window) -> Unit,
        private val levelsWindow: LevelsWindow,
        optionsWindow: OptionsWindow
) : Window("Menu", skin, "jelly"), LanguageAware {

    private val menuPlayButton = TextButton("Play", skin, "toggle-simple")
    private val menuOptionsButton = TextButton("Options", skin, "toggle-simple")
    private val menuExitButton = TextButton("Exit", skin)

    init {
        isMovable = false
        isModal = false

        // Common padding for control button cells.
        val buttonPad = Value.percentHeight(0.05f, this)

        // Button for showing level selection window.
        val playButton = menuPlayButton.apply {
            addListener(object : ClickListener() {
                override fun clicked(event: InputEvent, x: Float, y: Float) {
                    setActive(levelsWindow)
                }
            })
        }

        add(playButton)
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

        add(optionsButton)
                .padBottom(buttonPad)
                .fillX()
                .row()

        // Exit game button.
        val exitCell = add(menuExitButton.apply {
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
    }

    override fun updateLanguage(lang: Language) {
        titleLabel.setText(lang["menuWindowTitle"])
        menuPlayButton.setText(lang["menuPlayButton"])
        menuOptionsButton.setText(lang["menuOptionsButton"])
        menuExitButton.setText(lang["exitButton"])
    }
}