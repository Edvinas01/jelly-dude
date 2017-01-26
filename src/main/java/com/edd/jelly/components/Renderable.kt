package com.edd.jelly.components

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.g2d.TextureRegion

data class Renderable(val textureRegion: TextureRegion) : Component {

    companion object : ComponentResolver<Renderable>(Renderable::class.java)
}

val Entity.renderable: Renderable
    get() = Renderable[this]