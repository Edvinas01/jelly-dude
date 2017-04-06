package com.edd.jelly.behaviour.ui

import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.viewport.FitViewport

abstract class StagedScreen constructor(
        protected val camera: OrthographicCamera,
        batch: SpriteBatch
) : ScreenAdapter() {

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
}