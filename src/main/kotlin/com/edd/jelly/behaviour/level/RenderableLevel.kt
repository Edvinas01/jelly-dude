package com.edd.jelly.behaviour.level

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.maps.MapLayer
import com.badlogic.gdx.maps.tiled.TiledMap
import com.edd.jelly.behaviour.components.ComponentResolver

data class RenderableLevel(
        val tiledMap: TiledMap,
        val baseLayers: List<MapLayer>,
        val backgroundLayers: List<MapLayer>,
        val foregroundLayers: List<MapLayer>,
        var background: Texture? = null
) : Component {

    companion object : ComponentResolver<RenderableLevel>(RenderableLevel::class.java)
}