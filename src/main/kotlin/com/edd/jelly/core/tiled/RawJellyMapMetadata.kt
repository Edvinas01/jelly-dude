package com.edd.jelly.core.tiled

data class RawJellyMapMetadata(
        val description: String?,
        val texture: String?,
        val author: String?,
        val names: Map<String, String>?
)