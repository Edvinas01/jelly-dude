package com.edd.jelly.behaviour.physics

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.utils.ImmutableArray
import com.edd.jelly.behaviour.components.*
import com.edd.jelly.behaviour.physics.body.SoftBody
import com.edd.jelly.util.degrees
import com.google.inject.Inject

class PhysicsSynchronizationSystem @Inject constructor() : EntitySystem() {

    private lateinit var softBodyEntities: ImmutableArray<Entity>
    private lateinit var physicsEntities: ImmutableArray<Entity>

    override fun addedToEngine(engine: Engine) {
        softBodyEntities = engine.getEntitiesFor(Family.all(
                Transform::class.java,
                SoftBody::class.java
        ).get())

        physicsEntities = engine.getEntitiesFor(Family.all(
                Transform::class.java,
                Physics::class.java
        ).get())
    }

    override fun update(deltaTime: Float) {

        // Sync soft bodies.
        softBodyEntities.forEach {
            val transform = it.transform
            val bodies = SoftBody[it].bodies

            val (_, pos) = transform
            bodies.forEach {
                pos.add(it.position.x, it.position.y)
            }
            pos.set(pos.x / bodies.size, pos.y / bodies.size)
        }

        // Sync physics entities.
        physicsEntities.forEach {
            val transform = it.transform
            val body = it.physics.body

            transform.position.set(body.position.x, body.position.y)
            transform.rotation = body.angle.degrees
        }
    }
}