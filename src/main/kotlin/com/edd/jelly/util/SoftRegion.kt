package com.edd.jelly.util

import com.badlogic.gdx.graphics.g2d.PolygonRegion
import com.badlogic.gdx.graphics.g2d.TextureRegion

class SoftRegion(
        textureCoords: FloatArray,
        region: TextureRegion,
        vertices: FloatArray,
        indices: ShortArray
) : PolygonRegion(region, vertices, indices) {

    init {
        val u = region.u
        val v = region.v
        val uvWidth = region.u2 - u
        val uvHeight = region.v2 - v

        var i = 0
        while (i < textureCoords.size) {
            textureCoords[i] = u + uvWidth * (textureCoords[i])
            i++
            textureCoords[i] = v + uvHeight * (1 - textureCoords[i])
            i++
        }

        textureCoords.forEachIndexed { index, fl ->
            this@SoftRegion.textureCoords[index] = fl
        }
    }
}