package com.edd.jelly.behaviour.ui.screen.windows

import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.edd.jelly.behaviour.level.LoadNewLevelEvent
import com.edd.jelly.behaviour.ui.LoadGameScreenEvent
import com.edd.jelly.behaviour.ui.screen.LanguageAware
import com.edd.jelly.core.events.Messaging
import com.edd.jelly.core.resources.Language
import com.edd.jelly.core.tiled.JellyMapLoader

class LevelsWindow constructor(
        skin: Skin,
        jellyMapLoader: JellyMapLoader,
        messaging: Messaging
) : Window("Levels", skin, "jelly"), LanguageAware {

    init {
        isMovable = false
        isModal = false

        val levelContainer = Table()
                .top()
                .left()

        val elementPad = Value.percentWidth(0.01f, levelContainer)
        val elementWidth = Value.percentWidth(0.23f, levelContainer)

        for ((index, meta) in jellyMapLoader.metadata.values.withIndex()) {
            if (index > 0 && index % 4 == 0) {
                levelContainer.row()
            }

            // Details about the level.
            val levelDetails = Table()
            val tooltip = TextTooltip("${meta.author}\n${meta.description}", skin)

            // Level image.
            levelDetails
                    .add(Image(meta.texture).apply {
                        addListener(tooltip)
                    })
                    .fill()
                    .row()

            // Launch level button.
            val playCell = levelDetails.add(TextButton(meta.name, skin).apply {
                label.setWrap(true)
                label.setEllipsis(true)

                addListener(object : ClickListener() {
                    override fun clicked(event: InputEvent, x: Float, y: Float) {
                        messaging.send(LoadNewLevelEvent(meta.internalName))
                        messaging.send(LoadGameScreenEvent)
                    }
                })

                addListener(tooltip)
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
        add(ScrollPane(levelContainer, skin)).grow()
    }

    override fun updateLanguage(lang: Language) {
        titleLabel.setText(lang["levelsWindowTitle"])
    }
}