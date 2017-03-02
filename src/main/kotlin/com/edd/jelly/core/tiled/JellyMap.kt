package com.edd.jelly.core.tiled

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.maps.MapLayer
import com.badlogic.gdx.maps.tiled.TiledMap
import com.edd.jelly.behaviour.components.ComponentResolver

class JellyMap internal constructor(
        val tiledMap: TiledMap,
        val backgroundLayers: List<MapLayer>,
        val foregroundLayers: List<MapLayer>,
        val background: Texture?
) : Component {
    companion object : ComponentResolver<JellyMap>(JellyMap::class.java)
}