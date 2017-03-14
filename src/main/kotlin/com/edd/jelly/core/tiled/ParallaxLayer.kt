package com.edd.jelly.core.tiled

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.maps.MapLayer
import com.badlogic.gdx.math.Vector2

class ParallaxLayer(
        val offset: Vector2,
        val speed: Vector2,
        val clampTop: Boolean,
        val clampBottom: Boolean,
        val texture: Texture,
        val size: Vector2) : MapLayer()