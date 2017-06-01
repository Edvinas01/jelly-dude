package com.edd.jelly.behaviour.physics

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.utils.ImmutableArray
import com.edd.jelly.behaviour.physics.body.RigidBody
import com.edd.jelly.behaviour.position.Transform
import com.edd.jelly.behaviour.position.transform
import com.edd.jelly.behaviour.physics.body.SoftBody
import com.edd.jelly.behaviour.physics.body.rigidBody
import com.edd.jelly.behaviour.rendering.SoftRenderable
import com.edd.jelly.util.degrees
import com.edd.jelly.util.toVec2
import com.google.inject.Inject

class PhysicsSynchronizationSystem @Inject constructor() : EntitySystem() {

    private lateinit var softRenderables: ImmutableArray<Entity>
    private lateinit var softBodyEntities: ImmutableArray<Entity>
    private lateinit var physicsEntities: ImmutableArray<Entity>
    private lateinit var particleEntities: ImmutableArray<Entity>

    override fun addedToEngine(engine: Engine) {
        softRenderables = engine.getEntitiesFor(Family.all(
                SoftRenderable::class.java,
                Transform::class.java,
                SoftBody::class.java
        ).get())

        softBodyEntities = engine.getEntitiesFor(Family.all(
                Transform::class.java,
                SoftBody::class.java
        ).get())

        physicsEntities = engine.getEntitiesFor(Family.all(
                Transform::class.java,
                RigidBody::class.java
        ).get())

        particleEntities = engine.getEntitiesFor(Family.all(
                Particles::class.java,
                RigidBody::class.java
        ).get())
    }

    override fun update(deltaTime: Float) {
        syncSoftRenderables()
        syncParticles()

        // Sync physics entities.
        physicsEntities.forEach {
            val transform = it.transform
            val body = it.rigidBody.body

            transform.position.set(body.position.x, body.position.y)
            transform.rotation = body.angle.degrees
        }
    }

    /**
     * Sync soft renderable entities.
     */
    private fun syncSoftRenderables() {

        // Sync transform.
        softBodyEntities.forEach {
            val transform = it.transform
            val bodies = SoftBody[it].bodies

            val pos = transform.position

            // Calculate transform center.
            pos.setZero()
            bodies.forEach {
                pos.add(it.position.x, it.position.y)
            }
            pos.set(pos.x / bodies.size, pos.y / bodies.size)
        }

        softRenderables.forEach {
            val transform = it.transform
            val vertices = SoftRenderable[it].region.vertices
            val softBody = SoftBody[it]
            val bodies = softBody.bodies

            val center = transform.position.toVec2()
            bodies.forEachIndexed { i, b ->

                val pos = b.getLocalPoint(center).negateLocal()
                vertices[i * 2] = pos.x
                vertices[i * 2 + 1] = pos.y
            }
        }
    }

    /**
     * Synchronize particles.
     */
    private fun syncParticles() {
        particleEntities.forEach {
            val transform = it.transform
            val group = Particles.mapper[it].particleGroup

            transform.position.set(group.position.x, group.position.y)
            transform.rotation = group.angle.degrees
        }
    }
}