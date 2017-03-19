package com.edd.jelly.core.tiled

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.maps.MapLayer
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.math.Vector2
import com.edd.jelly.behaviour.components.ComponentResolver

class JellyMap internal constructor(
        val tiledMap: TiledMap,
        val backgroundLayers: Collection<MapLayer>,
        val foregroundLayers: Collection<MapLayer>,
        val background: Texture?,
        val spawn: Vector2
) : Component {
    companion object : ComponentResolver<JellyMap>(JellyMap::class.java)
}