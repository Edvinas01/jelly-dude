package com.edd.jelly.game.component

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.math.Vector2

data class Transform(val position: Vector2,
                     val rotation: Float = 0f) : Component {

    constructor(x: Number = 0,
                y: Number = 0,
                rotation: Number = 0) : this(
            Vector2(x.toFloat(),
                    y.toFloat()),
            rotation.toFloat())

    companion object : ComponentResolver<Transform>(Transform::class.java)
}