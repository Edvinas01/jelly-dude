package com.edd.jelly.core.tiled

import com.badlogic.gdx.graphics.g2d.TextureRegion

data class JellyMapMetadata(
        val internalName: String,
        val description: String,
        val author: String,
        val name: String,
        val texture: TextureRegion
)