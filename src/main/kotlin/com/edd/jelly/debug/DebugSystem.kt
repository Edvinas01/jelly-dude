package com.edd.jelly.debug

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.edd.jelly.core.configuration.ConfigChangedEvent
import com.edd.jelly.core.configuration.Configurations
import com.edd.jelly.core.events.Listener
import com.edd.jelly.core.events.Messaging
import com.google.inject.Inject

class DebugSystem @Inject constructor(
        private val shapeRenderer: ShapeRenderer,
        private val messaging: Messaging,
        private val camera: OrthographicCamera,
        configurations: Configurations
) : EntitySystem() {

    private val game = configurations.config.game

    override fun addedToEngine(engine: Engine?) {
        super.addedToEngine(engine)

        setProcessing(game.debug)
        messaging.listen(object : Listener<ConfigChangedEvent> {
            override fun listen(event: ConfigChangedEvent) {
                setProcessing(event.config.game.debug)
            }
        })
    }

    override fun update(deltaTime: Float) {
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