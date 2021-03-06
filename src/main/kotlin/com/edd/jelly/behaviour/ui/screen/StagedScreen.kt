package com.edd.jelly.behaviour.ui.screen

import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.viewport.FitViewport
import com.edd.jelly.core.resources.Language

/**
 * Root UI screen class for the game. Used to reduce boilerplate.
 */
abstract class StagedScreen constructor(
        protected val camera: OrthographicCamera,
        batch: SpriteBatch
) : ScreenAdapter(), LanguageAware {

    val stage: Stage = Stage(FitViewport(camera.viewportWidth, camera.viewportHeight), batch)

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

    override fun updateLanguage(lang: Language) {
    }
}