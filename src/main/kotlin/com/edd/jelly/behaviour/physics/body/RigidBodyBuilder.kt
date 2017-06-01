package com.edd.jelly.behaviour.physics.body

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.maps.MapObject
import com.edd.jelly.core.resources.ResourceManager
import com.google.inject.Inject
import com.google.inject.Singleton
import org.jbox2d.dynamics.World

/**
 * Builds single rigid bodies.
 */
@Singleton
class RigidBodyBuilder @Inject constructor(
        private val resources: ResourceManager,
        private val world: World
) : BodyBuilder {

    override fun create(mapObject: MapObject): Entity? {
        return null
    }
}