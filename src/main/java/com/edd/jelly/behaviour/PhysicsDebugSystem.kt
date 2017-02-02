package com.edd.jelly.behaviour

import com.badlogic.ashley.core.EntitySystem
import com.badlogic.gdx.graphics.Camera
import com.edd.jelly.util.DebugRenderer
import com.google.inject.Inject
import org.jbox2d.dynamics.World

class PhysicsDebugSystem @Inject constructor(private val renderer: DebugRenderer,
                                             private val camera: Camera,
                                             private val world: World) : EntitySystem() {

    override fun update(deltaTime: Float) {
        renderer.render(world, camera.combined)
    }
}