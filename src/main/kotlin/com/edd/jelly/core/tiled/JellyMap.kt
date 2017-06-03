package com.edd.jelly.core.tiled

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.maps.MapLayer
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.math.Vector2
import com.edd.jelly.util.ComponentResolver

class JellyMap internal constructor(
        val name: String,
        val width: Float,
        val height: Float,
        val tiledMap: TiledMap,
        val backgroundLayers: Collection<MapLayer>,
        val foregroundLayers: Collection<MapLayer>,
        val background: Texture?,
        val playerTexture: TextureRegion?,
        val spawn: Vector2?,
        val focusPoints: List<Vector2>,
        val collisionsLayer: MapLayer,
        val entitiesLayer: MapLayer,
        val ambientSoundNames: List<String>,
        val musicNames: List<String>
) : Component {
    companion object : ComponentResolver<JellyMap>(JellyMap::class.java)
}