package com.edd.jelly.core.tiled

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.maps.MapLayer

class ParallaxLayer(
        val offsetX: Float,
        val offsetY: Float,
        val speedX: Float,
        val speedY: Float,
        val texture: Texture) : MapLayer()