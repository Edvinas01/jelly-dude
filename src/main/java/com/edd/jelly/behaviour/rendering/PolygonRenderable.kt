package com.edd.jelly.behaviour.rendering

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.graphics.g2d.PolygonRegion
import com.edd.jelly.behaviour.components.ComponentResolver

data class PolygonRenderable(
        val polygonRegion: PolygonRegion
) : Component {

    companion object : ComponentResolver<PolygonRenderable>(PolygonRenderable::class.java)
}