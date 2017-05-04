package com.edd.jelly.behaviour.position

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.math.Vector2
import com.edd.jelly.util.ComponentResolver

data class Transform(val position: Vector2 = Vector2(0f, 0f),
                     val size: Vector2 = Vector2(0f, 0f),
                     val scale: Vector2 = Vector2(1f, 1f),
                     var rotation: Float = 0f) : Component {

    companion object : ComponentResolver<Transform>(Transform::class.java)

    var x: Float
        get() = position.x
        set(value) {
            position.x = value
        }

    var y: Float
        get() = position.y
        set(value) {
            position.y = value
        }

    var width: Float
        get() = size.x
        set(value) {
            size.x = value
        }

    var height: Float
        get() = size.y
        set(value) {
            size.y = value
        }
}

val Entity.transform: Transform
    get() = Transform[this]