package com.edd.jelly.behaviour.physics.body

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.maps.MapLayer
import com.badlogic.gdx.maps.MapObjects
import com.edd.jelly.core.tiled.string
import com.google.inject.Inject
import com.google.inject.Singleton

@Singleton
class BodyEntityFactory @Inject constructor(
        private val rigidBodyGroupBuilder: RigidBodyGroupBuilder,
        private val softBodyBuilder: SoftBodyBuilder,
        private val mapBodyBuilder: MapBodyBuilder,
        private val liquidBuilder: LiquidBuilder
) {

    private companion object {
        const val TYPE = "type"

        const val TYPE_RIGID_GROUP = "rigid_group"
        const val TYPE_CHECKPOINT = "checkpoint"
        const val TYPE_LIQUID = "liquid"
        const val TYPE_SOFT = "soft"
    }

    /**
     * Create list of entity bodes based on provided map layer objects.
     */
    fun create(layer: MapLayer): List<Entity> {
        return create(layer.objects)
    }

    /**
     * Create list of entity bodies based on provided map objects.
     */
    fun create(mapObjects: MapObjects): List<Entity> {
        return mapObjects.map { o ->
            when (o.string(TYPE)) {
                TYPE_RIGID_GROUP -> rigidBodyGroupBuilder.create(o)
                TYPE_CHECKPOINT -> null
                TYPE_LIQUID -> liquidBuilder.create(o)
                TYPE_SOFT -> softBodyBuilder.create(o)
                else -> mapBodyBuilder.create(o)
            }
        }.filterNotNull()
    }
}