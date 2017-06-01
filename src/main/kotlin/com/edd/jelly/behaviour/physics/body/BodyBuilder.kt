package com.edd.jelly.behaviour.physics.body

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.maps.MapObject

interface BodyBuilder {

    fun create(mapObject: MapObject): Entity?
}