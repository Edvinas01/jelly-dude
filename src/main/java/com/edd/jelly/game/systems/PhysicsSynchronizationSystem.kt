package com.edd.jelly.game.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.systems.IteratingSystem
import com.edd.jelly.game.components.Physics
import com.edd.jelly.game.components.Transform
import com.edd.jelly.game.components.physics
import com.edd.jelly.game.components.transform
import com.edd.jelly.util.degrees
import com.google.inject.Inject

class PhysicsSynchronizationSystem @Inject constructor() : IteratingSystem(Family.all(
        Transform::class.java,
        Physics::class.java).get()) {

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val transform = entity.transform
        val body = entity.physics.body

        transform.position.set(body.position)
        transform.rotation = body.angle.degrees
    }
}