package com.edd.jelly.behaviour.ui.screen.windows

import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.Scaling
import com.edd.jelly.behaviour.common.event.ErrorInfoEvent
import com.edd.jelly.behaviour.common.event.LoadNewLevelEvent
import com.edd.jelly.behaviour.common.event.LoadGameScreenEvent
import com.edd.jelly.behaviour.ui.screen.LanguageAware
import com.edd.jelly.core.events.Messaging
import com.edd.jelly.core.resources.Language
import com.edd.jelly.core.tiled.JellyMapLoader
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class LevelsWindow constructor(
        private val jellyMapLoader: JellyMapLoader,
        skin: Skin,
        messaging: Messaging,
        language: String
) : Window("Levels", skin, "jelly"), LanguageAware {

    private companion object {
        val LOG: Logger = LogManager.getLogger(LevelsWindow::class.java)
    }

    private val playButtons = mutableMapOf<String, TextButton>()

    init {
        isMovable = false
        isModal = false

        val levelContainer = Table()
                .top()
                .left()

        val elementPad = Value.percentWidth(0.01f, levelContainer)
        val elementWidth = Value.percentWidth(0.23f, levelContainer)
        val maxImgHeight = Value.percentWidth(1f, this)

        val cols = 4

        for ((index, meta) in jellyMapLoader.metadata.values.withIndex()) {
            if (index > 0 && index % cols == 0) {
                levelContainer.row()
            }

            // Details about the level.
            val levelDetails = Table()
            val tooltip = TextTooltip("${meta.author}\n${meta.description}", skin)

            // Level image.
            levelDetails
                    .add(Image(meta.texture).apply {
                        addListener(tooltip)
                        setScaling(Scaling.fit)
                    })
                    .maxHeight(maxImgHeight)
                    .prefHeight(maxImgHeight)
                    .grow()
                    .row()

            val button = TextButton(meta.names[language] ?: meta.internalName.capitalize(), skin).apply {
                label.setWrap(true)
                label.setEllipsis(true)

                addListener(object : ClickListener() {
                    override fun clicked(event: InputEvent, x: Float, y: Float) {

                        // Level loading might fail, messaging is not async, so gotta handle it here.
                        // Don't want to catch 5+ errors from Gdx, so just using a runtime error.
                        @Suppress("CatchRuntimeException")
                        try {
                            messaging.send(LoadNewLevelEvent(meta.internalName))
                            messaging.send(LoadGameScreenEvent)
                        } catch (e: RuntimeException) {
                            LOG.error("Could not load level", e)

                            // Try to recover.
                            messaging.send(ErrorInfoEvent(e.message ?: "Could not load level"))
                        }
                    }
                })

                addListener(tooltip)
            }

            // Launch level button.
            levelDetails
                    .add(button)
                    .growX()

            playButtons.put(meta.internalName, button)

            levelContainer
                    .add(levelDetails)
                    .padBottom(8f)
                    .padLeft(elementPad)
                    .padRight(elementPad)
                    .width(elementWidth)
                    .height(elementWidth)
        }

        // Scroll for levels.
        add(ScrollPane(levelContainer, skin)).grow()
    }

    override fun updateLanguage(lang: Language) {
        titleLabel.setText(lang["levelsWindowTitle"])

        playButtons.forEach { internalName, button ->
            button.setText(jellyMapLoader.metadata[internalName]
                    ?.names
                    ?.get(lang.handle.internalName)
                    ?: internalName.capitalize()
            )
        }
    }
}