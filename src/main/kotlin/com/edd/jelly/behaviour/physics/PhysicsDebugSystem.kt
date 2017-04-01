package com.edd.jelly.behaviour.physics

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.gdx.graphics.OrthographicCamera
import com.edd.jelly.core.configuration.ConfigChangedEvent
import com.edd.jelly.core.configuration.Configurations
import com.edd.jelly.core.events.Listener
import com.edd.jelly.core.events.Messaging
import com.google.inject.Inject
import org.jbox2d.dynamics.World

class PhysicsDebugSystem @Inject constructor(private val messaging: Messaging,
                                             private val renderer: DebugRenderer,
                                             private val camera: OrthographicCamera,
                                             private val world: World,
                                             configurations: Configurations) : EntitySystem() {

    private val game = configurations.config.game

    override fun addedToEngine(engine: Engine?) {
        super.addedToEngine(engine)

        setProcessing(game.debug)

        messaging.listen<ConfigChangedEvent> {
            setProcessing(it.config.game.debug)
        }
    }

    override fun update(deltaTime: Float) {
        renderer.render(world, camera.combined)
    }
}