package com.edd.jelly.behaviour.rendering

import com.badlogic.ashley.core.Component
import com.edd.jelly.util.ComponentResolver
import com.edd.jelly.util.SoftRegion

data class SoftRenderable(
        val region: SoftRegion,
        val offset: Float = 0f
) : Component {

    companion object : ComponentResolver<SoftRenderable>(SoftRenderable::class.java)
}