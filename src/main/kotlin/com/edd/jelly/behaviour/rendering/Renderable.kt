package com.edd.jelly.behaviour.rendering

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.edd.jelly.util.ComponentResolver

data class Renderable(val textureRegion: TextureRegion) : Component {

    companion object : ComponentResolver<Renderable>(Renderable::class.java)
}