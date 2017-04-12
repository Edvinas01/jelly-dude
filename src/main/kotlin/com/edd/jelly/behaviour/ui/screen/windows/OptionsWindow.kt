package com.edd.jelly.behaviour.ui.screen.windows

import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.ui.Value.percentWidth
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
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

    private val languageLabel = Label("Language", skin)

    private val languageSelect = SelectBox<LanguageHandle>(skin).apply {
        val langArray = Array<LanguageHandle>()
        languages.forEach {
            langArray.add(it.handle)
        }

        items = langArray
        selected = language.handle
    }

    private val fullscreenCheckbox = CheckBox("Fullscreen", skin).apply {
        isChecked = configurations.config.video.screen.fullscreen
    }

    private val saveButton = TextButton("Accept", skin).apply {
        addListener(object : ClickListener() {
            override fun clicked(event: InputEvent, x: Float, y: Float) {
                val config = configurations.config
                config.game.language = languageSelect.selected.internalName
                config.video.screen.fullscreen = fullscreenCheckbox.isChecked
                configurations.save()
            }
        })
    }

    init {
        isMovable = false
        isModal = false

        val width = percentWidth(0.25f, this)

        // Add language group to options.
        add(Table().apply {
            add(languageLabel)
                    .growX()
                    .row()

            add(languageSelect)
                    .growX()
        }).width(width)

        // Enable or disable full screen.
        add(fullscreenCheckbox).width(width)

        row()

        add(saveButton)
                .colspan(2)
                .expand()
                .bottom()
                .left()
    }

    override fun updateLanguage(lang: Language) {
        titleLabel.setText(lang["optionsWindowTitle"])
        languageLabel.setText(lang["optionsLanguageLabel"])
        fullscreenCheckbox.setText(lang["optionsFullscreenLabel"])
        saveButton.setText(lang["saveButton"])
    }
}