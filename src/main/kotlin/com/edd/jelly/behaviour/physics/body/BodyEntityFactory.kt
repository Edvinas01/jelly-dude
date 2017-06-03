package com.edd.jelly.behaviour.physics.body

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.maps.MapLayer
import com.badlogic.gdx.maps.MapObjects
import com.edd.jelly.core.tiled.string
import com.google.inject.Inject
import com.google.inject.Singleton
import kotlin.reflect.KClass

@Singleton
class BodyEntityFactory @Inject constructor(
        private val rigidBodyBuilder: RigidBodyBuilder,
        private val softBodyBuilder: SoftBodyBuilder,
        private val mapBodyBuilder: MapBodyBuilder,
        private val liquidBuilder: LiquidBuilder
) {

    private companion object {
        const val TYPE = "type"

        const val TYPE_LIQUID = "liquid"
        const val TYPE_RIGID = "rigid"
        const val TYPE_SOFT = "soft"
    }

    /**
     * Create list of entity bodes based on provided map layer objects.
     */
    fun create(layer: MapLayer): List<Entity> {
        return create(layer.objects)
    }

    /**
     * Create list of entity bodes based on provided map layer objects and builder type.
     */
    fun createWith(type: KClass<out BodyBuilder>, layer: MapLayer): List<Entity> {
        val builder = when (type) {
            RigidBodyBuilder::class -> rigidBodyBuilder
            SoftBodyBuilder::class -> softBodyBuilder
            MapBodyBuilder::class -> mapBodyBuilder
            LiquidBuilder::class -> liquidBuilder
            else -> NullBuilder
        }

        return layer.objects.map { o ->
            builder.create(o)
        }.filterNotNull()
    }

    /**
     * Create list of entity bodies based on provided map objects.
     */
    fun create(mapObjects: MapObjects): List<Entity> {
        return mapObjects.map { o ->
            when (o.string(TYPE)) {
                TYPE_LIQUID -> liquidBuilder.create(o)
                TYPE_RIGID -> rigidBodyBuilder.create(o)
                TYPE_SOFT -> softBodyBuilder.create(o)
                else -> NullBuilder.create(o)
            }
        }.filterNotNull()
    }
}