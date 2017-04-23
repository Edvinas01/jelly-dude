package com.edd.jelly.behaviour.physics

import com.badlogic.ashley.core.*
import com.edd.jelly.behaviour.physics.contacts.MessagingContactListener
import com.edd.jelly.behaviour.pause.PausingSystem
import com.edd.jelly.behaviour.physics.body.SoftBody
import com.edd.jelly.util.EntityListenerAdapter
import com.google.inject.Inject
import org.jbox2d.dynamics.World

class PhysicsSystem @Inject constructor(
        private val messagingContactListener: MessagingContactListener,
        private val world: World
) : EntitySystem(), PausingSystem {

    companion object {
        private val VELOCITY_ITERATIONS = 6
        private val POSITION_ITERATIONS = 2
        private val MIN_FRAME_TIME = 0.25f
        private val TIME_STEP = 1.0f / 300f
    }

    private var accumulator = 0f

    override fun addedToEngine(engine: Engine) {
        super.addedToEngine(engine)

        world.setContactListener(messagingContactListener)

        // Listen for soft body physics objects.
        engine.addEntityListener(Family.all(SoftBody::class.java).get(), object : EntityListenerAdapter() {
            override fun entityRemoved(entity: Entity) {
                SoftBody[entity].bodies.forEach {
                    world.destroyBody(it)
                }
            }
        })

        // Listen for destroyed physics objects.
        engine.addEntityListener(Family.all(Physics::class.java).get(), object : EntityListenerAdapter() {
            override fun entityRemoved(entity: Entity) {
                world.destroyBody(entity.physics.body)
            }
        })

        // Listen for destroyed particle objects.
        engine.addEntityListener(Family.all(Particles::class.java).get(), object : EntityListenerAdapter() {
            override fun entityRemoved(entity: Entity) {
                world.destroyParticlesInGroup(Particles.mapper[entity].particleGroup)
            }
        })
    }

    override fun update(deltaTime: Float) {
        val frameTime = Math.min(deltaTime, MIN_FRAME_TIME)

        accumulator += frameTime
        while (accumulator >= TIME_STEP) {
            world.step(TIME_STEP, VELOCITY_ITERATIONS, POSITION_ITERATIONS)
            accumulator -= TIME_STEP
        }
    }
}