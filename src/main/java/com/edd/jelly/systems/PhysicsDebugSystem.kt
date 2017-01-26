package com.edd.jelly.systems

import com.badlogic.ashley.core.EntitySystem
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer
import com.badlogic.gdx.physics.box2d.World
import com.google.inject.Inject

class PhysicsDebugSystem @Inject constructor(private val renderer: Box2DDebugRenderer,
                                             private val camera: Camera,
                                             private val world: World) : EntitySystem() {

    override fun update(deltaTime: Float) {
        renderer.render(world, camera.combined)
    }
}