package com.edd.jelly.behaviour.physics

import com.badlogic.ashley.core.EntitySystem
import com.badlogic.gdx.graphics.OrthographicCamera
import com.edd.jelly.behaviour.physics.DebugRenderer
import com.google.inject.Inject
import org.jbox2d.dynamics.World

class PhysicsDebugSystem @Inject constructor(private val renderer: DebugRenderer,
                                             private val camera: OrthographicCamera,
                                             private val world: World) : EntitySystem() {

    override fun update(deltaTime: Float) {
        renderer.render(world, camera.combined)
    }
}