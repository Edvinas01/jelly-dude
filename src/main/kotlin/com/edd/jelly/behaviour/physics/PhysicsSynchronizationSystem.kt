package com.edd.jelly.behaviour.physics

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.utils.ImmutableArray
import com.edd.jelly.behaviour.components.*
import com.edd.jelly.behaviour.physics.body.SoftBody
import com.edd.jelly.behaviour.rendering.SoftRenderable
import com.edd.jelly.util.degrees
import com.edd.jelly.util.toVec2
import com.google.inject.Inject
import org.jbox2d.common.Vec2

class PhysicsSynchronizationSystem @Inject constructor() : EntitySystem() {

    private lateinit var softRenderables: ImmutableArray<Entity>
    private lateinit var softBodyEntities: ImmutableArray<Entity>
    private lateinit var physicsEntities: ImmutableArray<Entity>

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
                Physics::class.java
        ).get())
    }

    override fun update(deltaTime: Float) {

        // Order of these methods is important!
        syncSoftBodies()
        syncSoftRenderables()

        // Sync physics entities.
        physicsEntities.forEach {
            val transform = it.transform
            val body = it.physics.body

            transform.position.set(body.position.x, body.position.y)
            transform.rotation = body.angle.degrees
        }
    }

    /**
     * Synchronizes soft-bodies.
     */
    private fun syncSoftBodies() {
        softBodyEntities.forEach {
            val transform = it.transform
            val bodies = SoftBody[it].bodies

            val (pos) = transform

            // Calculate transform center.
            pos.setZero()
            bodies.forEach {
                pos.add(it.position.x, it.position.y)
            }
            pos.set(pos.x / bodies.size, pos.y / bodies.size)


        }
    }

    /**
     * Sync soft renderable entities.
     */
    private fun syncSoftRenderables() {
        softRenderables.forEach {
            val transform = it.transform
            val (region, offset) = SoftRenderable[it]
            val vertices = region.vertices
            val bodies = SoftBody[it].bodies

            val center = transform.position.toVec2()
            bodies.forEachIndexed { i, b ->
                val pos = b.getLocalPoint(center)

                vertices[i * 2] = -pos.x
                vertices[i * 2 + 1] = -pos.y
            }
        }
    }
}