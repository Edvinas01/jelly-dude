package com.edd.jelly.behaviour.physics.body

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.maps.MapObject

object NullBuilder : BodyBuilder {
    override fun create(mapObject: MapObject): Entity? = null
}