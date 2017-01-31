package com.edd.jelly.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.systems.IteratingSystem
import com.edd.jelly.components.*
import com.edd.jelly.util.degrees
import com.google.inject.Inject

class ParticleSynchronizationSystem @Inject constructor() : IteratingSystem(Family.all(
        Particles::class.java,
        Physics::class.java).get()) {

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val transform = entity.transform
        val group = entity.particles.particleGroup

        transform.position.set(group.position.x, group.position.y)
        transform.rotation = group.angle.degrees
    }
}