package com.edd.jelly.behaviour

import com.badlogic.ashley.core.EntitySystem
import com.google.inject.Inject
import org.jbox2d.dynamics.World

class PhysicsSystem @Inject constructor(private val world: World) : EntitySystem() {

    companion object {
        private val VELOCITY_ITERATIONS = 6
        private val POSITION_ITERATIONS = 2
        private val MIN_FRAME_TIME = 0.25f
        private val TIME_STEP = 1.0f / 300f
    }

    private var accumulator = 0f

    override fun update(deltaTime: Float) {
        val frameTime = Math.min(deltaTime, MIN_FRAME_TIME)

        accumulator += frameTime
        while (accumulator >= TIME_STEP) {
            world.step(TIME_STEP, VELOCITY_ITERATIONS, POSITION_ITERATIONS)
            accumulator -= TIME_STEP
        }
    }
}