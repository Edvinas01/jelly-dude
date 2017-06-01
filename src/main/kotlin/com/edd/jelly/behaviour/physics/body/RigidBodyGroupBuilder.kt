package com.edd.jelly.behaviour.physics.body

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.maps.MapObject
import com.edd.jelly.core.resources.ResourceManager
import com.google.inject.Inject
import com.google.inject.Singleton
import org.jbox2d.dynamics.World

/**
 * Builds rigid body groups.
 */
@Singleton
class RigidBodyGroupBuilder @Inject constructor(
        private val rigidBodyBuilder: RigidBodyBuilder,
        private val resources: ResourceManager,
        private val world: World
) : BodyBuilder {

    override fun create(mapObject: MapObject): Entity? {
        return null
    }
}