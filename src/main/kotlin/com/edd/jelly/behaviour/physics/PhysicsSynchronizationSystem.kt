package com.edd.jelly.behaviour.physics

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.utils.ImmutableArray
import com.badlogic.gdx.math.MathUtils
import com.edd.jelly.behaviour.components.Transform
import com.edd.jelly.behaviour.components.transform
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

//            atan2(P2.y - P1.y, P2.x - P1.x) - atan2(P3.y - P1.y, P3.x - P1.x)

            val p1 = softBody.pivots[2].getLocalPoint(center)
            val p2 = softBody.pivots[0].getLocalPoint(center)
            val p3 = softBody.pivots[1].getLocalPoint(center)

            val angle = (MathUtils.atan2(p2.y - p1.y, p2.x - p1.x) - MathUtils.atan2(p3.y - p1.y, p3.x - p1.x)) * MathUtils.radiansToDegrees
            println(angle)

            bodies.forEachIndexed { i, b ->

                val pos = b.getLocalPoint(center).negateLocal()
                vertices[i * 2] = pos.x
                vertices[i * 2 + 1] = pos.y
            }
        }
    }
}