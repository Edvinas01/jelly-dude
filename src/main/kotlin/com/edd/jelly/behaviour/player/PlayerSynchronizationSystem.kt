package com.edd.jelly.behaviour.player

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.math.EarClippingTriangulator
import com.badlogic.gdx.math.Vector2
import com.edd.jelly.behaviour.components.Transform
import com.edd.jelly.behaviour.components.transform
import com.edd.jelly.behaviour.rendering.PolygonRenderable
import com.edd.jelly.util.pixels
import com.google.inject.Inject
import org.jbox2d.common.Vec2

class PlayerSynchronizationSystem @Inject constructor(
        private val triangulator: EarClippingTriangulator
) : IteratingSystem(Family.all(
        PolygonRenderable::class.java,
        Transform::class.java,
        Player::class.java
).get()) {

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val player = Player.mapper[entity]
        val bodies = player.joint.bodies

        // Adjust player transform, gotta sync it first since further calculations require it to be in position. Thus
        // two iterations of bodies are required. Can't think of a better way right now.
        val midPoint = Vector2()
        bodies.forEach { body ->
            midPoint.x += body.position.x
            midPoint.y += body.position.y

            player.velocity.x += body.linearVelocity.x
            player.velocity.y += body.linearVelocity.y
        }

        val transform = entity.transform

        // Count average player position.
        transform.x = midPoint.x / bodies.size
        transform.y = midPoint.y / bodies.size

        // Count average player velocity.
        player.velocity.x = player.velocity.x / bodies.size
        player.velocity.y = player.velocity.y / bodies.size

        // Center point of the player.
        val vec = Vec2(
                transform.position.x - transform.width / 2,
                transform.position.y - transform.height / 2)

        val polygonRegion = PolygonRenderable.mapper[entity].polygonRegion
        val region = polygonRegion.region

        // Difference ration between texture size and actual player size.
        val xRatio = region.regionWidth / transform.width.pixels
        val yRatio = region.regionHeight / transform.height.pixels

        // Adjust player texture vertices, staying in pixels since I want the rendering system to say
        // consistent for all polygon regions.
        bodies.forEachIndexed { i, body ->
            polygonRegion.vertices[i * 2] = -body.getLocalPoint(vec).x.pixels * xRatio
            polygonRegion.vertices[i * 2 + 1] = -body.getLocalPoint(vec).y.pixels * yRatio
        }

        // Triangles also have to be recalculated.
        triangulator.computeTriangles(polygonRegion.vertices).toArray().forEachIndexed { i, triangle ->
            polygonRegion.triangles[i] = triangle
        }
    }
}