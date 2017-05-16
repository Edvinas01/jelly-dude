package com.edd.jelly.behaviour.ui.screen.windows

import com.badlogic.gdx.Input
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.ui.Value.percentWidth
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.Array
import com.edd.jelly.behaviour.ui.screen.LanguageAware
import com.edd.jelly.core.configuration.Configurations
import com.edd.jelly.core.resources.Language
import com.edd.jelly.core.resources.LanguageHandle

class OptionsWindow constructor(
        private val configurations: Configurations,
        skin: Skin,
        languages: Collection<Language>,
        language: Language
) : Window("Options", skin, "jelly"), LanguageAware {

    private val input = configurations.config.input

    // Inputs.
    private val upInputLabel = Label("Up", skin)
    private val upInput = KeyTextField(input.up)

    private val downInputLabel = Label("Down", skin)
    private val downInput = KeyTextField(input.down)

    private val leftInputLabel = Label("Left", skin)
    private val leftInput = KeyTextField(input.left)

    private val rightInputLabel = Label("Right", skin)
    private val rightInput = KeyTextField(input.right)

    private val resetInputLabel = Label("Reset", skin)
    private val resetInput = KeyTextField(input.reset)

    private val stickInputLabel = Label("Stick", skin)
    private val stickInput = KeyTextField(input.stick)

    private val shrinkInputLabel = Label("Shrink", skin)
    private val shrinkInput = KeyTextField(input.shrink)

    // General.
    private val languageLabel = Label("Language", skin)
    private val languageSelect = SelectBox<LanguageHandle>(skin).apply {
        val langArray = Array<LanguageHandle>()
        languages.forEach {
            langArray.add(it.handle)
        }

        items = langArray
        selected = language.handle
    }

    /**
     * Listener which fixes sliders which are placed within scroll pane.
     */
    private val sliderFixingListener = object : InputListener() {
        override fun touchDown(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int): Boolean {
            event.stop()
            return false
        }
    }

    private val soundSliderLabel = Label("Sound", skin)
    private val soundSlider = Slider(0f, 1f, 0.05f, false, skin).apply {
        value = configurations.config.game.soundVolume
        addListener(sliderFixingListener)
    }

    private val musicSliderLabel = Label("Music", skin)
    private val musicSlider = Slider(0f, 1f, 0.05f, false, skin).apply {
        value = configurations.config.game.musicVolume
        addListener(sliderFixingListener)
    }

    // Checkboxes.
    private val fullscreenCheckbox = CheckBox("Fullscreen", skin).apply {
        isChecked = configurations.config.video.screen.fullscreen
    }

    private val scriptingCheckbox = CheckBox("Scripting", skin).apply {
        isChecked = configurations.config.game.scripting
    }

    private val debugCheckbox = CheckBox("Debug", skin).apply {
        isChecked = configurations.config.game.debug
    }

    private val saveButton = TextButton("Accept", skin).apply {
        addListener(object : ClickListener() {
            override fun clicked(event: InputEvent, x: Float, y: Float) {
                val config = configurations.config
                val input = config.input
                val game = config.game

                config.video.screen.fullscreen = fullscreenCheckbox.isChecked

                input.up = upInput.text
                input.down = downInput.text
                input.left = leftInput.text
                input.right = rightInput.text
                input.reset = resetInput.text
                input.stick = stickInput.text
                input.shrink = shrinkInput.text

                game.language = languageSelect.selected.internalName
                game.soundVolume = soundSlider.value
                game.musicVolume = musicSlider.value
                game.scripting = scriptingCheckbox.isChecked
                game.debug = debugCheckbox.isChecked

                configurations.save()
            }
        })
    }

    init {
        isMovable = false
        isModal = false

        val desiredWidth = percentWidth(0.32f, this)
        val padRight = percentWidth(0.02f, this)

        val options = Table().left()

        // Add input options.
        options.add(Table().apply {

            // Up.
            add(upInputLabel)
                    .growX()
                    .row()

            add(upInput)
                    .growX()
                    .row()

            // Down.
            add(downInputLabel)
                    .growX()
                    .row()

            add(downInput)
                    .growX()
                    .row()

            // Left.
            add(leftInputLabel)
                    .growX()
                    .row()

            add(leftInput)
                    .growX()
                    .row()

            // Right.
            add(rightInputLabel)
                    .growX()
                    .row()

            add(rightInput)
                    .growX()
                    .row()

            // Reset.
            add(resetInputLabel)
                    .growX()
                    .row()

            add(resetInput)
                    .growX()
                    .row()

            // Stick.
            add(stickInputLabel)
                    .growX()
                    .row()

            add(stickInput)
                    .growX()
                    .row()

            // Shrink.
            add(shrinkInputLabel)
                    .growX()
                    .row()

            add(shrinkInput).growX()
        }).top().padRight(padRight)
                .width(desiredWidth)

        // Add language and sound group to options.
        options.add(Table().apply {
            add(languageLabel)
                    .growX()
                    .row()

            add(languageSelect)
                    .growX()
                    .row()

            // Sound volume.
            add(soundSliderLabel)
                    .growX()
                    .row()

            add(soundSlider)
                    .growX()
                    .row()

            // Music volume.
            add(musicSliderLabel)
                    .growX()
                    .row()

            add(musicSlider)
                    .growX()

        }).top().padRight(padRight)
                .width(desiredWidth)

        // Checkbox options.
        options.add(Table().apply {
            add(fullscreenCheckbox)
                    .left()
                    .row()

            add(scriptingCheckbox)
                    .left()
                    .row()

            add(debugCheckbox)
                    .left()

        }).top()

        add(ScrollPane(options, skin))
                .grow()
                .top()
                .left()

        row()

        add(saveButton)
                .expand()
                .bottom()
                .left()
    }

    override fun updateLanguage(lang: Language) {
        titleLabel.setText(lang["optionsWindowTitle"])

        languageLabel.setText(lang["optionsLanguageLabel"])
        soundSliderLabel.setText(lang["optionsSoundLabel"])
        musicSliderLabel.setText(lang["optionsMusicLabel"])

        upInputLabel.setText(lang["optionsUpLabel"])
        downInputLabel.setText(lang["optionsDownLabel"])
        leftInputLabel.setText(lang["optionsLeftLabel"])
        rightInputLabel.setText(lang["optionsRightLabel"])
        resetInputLabel.setText(lang["optionsResetLabel"])
        stickInputLabel.setText(lang["optionsStickLabel"])
        shrinkInputLabel.setText(lang["optionsShrinkLabel"])

        fullscreenCheckbox.setText(lang["optionsFullscreenLabel"])
        scriptingCheckbox.setText(lang["optionsScriptingLabel"])
        debugCheckbox.setText(lang["optionsDebugLabel"])

        saveButton.setText(lang["saveButton"])
    }

    inner class KeyTextField(key: String) : TextField(key, skin) {

        init {
            addListener(object : TextFieldClickListener() {
                override fun keyTyped(event: InputEvent, character: Char): Boolean {
                    setText(Input.Keys.toString(event.keyCode))
                    return true
                }
            })
        }
    }
}