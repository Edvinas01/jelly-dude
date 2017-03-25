package com.edd.jelly.debug

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.edd.jelly.core.configuration.Configurations
import com.google.inject.Inject

class DebugSystem @Inject constructor(
        private val shapeRenderer: ShapeRenderer,
        private val camera: OrthographicCamera,
        configurations: Configurations
) : EntitySystem() {

    private val game = configurations.config.game

    override fun addedToEngine(engine: Engine?) {
        super.addedToEngine(engine)
    }

    override fun update(deltaTime: Float) {
        if (!game.debug) {
            return
        }

        with(camera.position) {
            shapeRenderer.projectionMatrix = camera.combined
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line)

            val hw = camera.viewportWidth / 2
            val hh = camera.viewportHeight / 2

            shapeRenderer.color = Color.GOLD

            shapeRenderer.line(x - hw / 2, y, 0f, x + hw / 2, y, 0f)
            shapeRenderer.line(x, y - hh / 2, 0f, x, y + hh / 2, 0f)

            shapeRenderer.rect(x - hw, y - hh, camera.viewportWidth, camera.viewportHeight)
            shapeRenderer.end()
        }
    }
}