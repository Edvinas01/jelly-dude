package com.edd.jelly.behaviour.level

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.maps.tiled.TiledMap
import com.edd.jelly.behaviour.components.ComponentResolver

data class RenderableLevel(
        val tiledMap: TiledMap
) : Component {

    companion object : ComponentResolver<RenderableLevel>(RenderableLevel::class.java)
}