package com.edd.jelly.util

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import org.jbox2d.common.Vec2

fun Vector2.toVec2() = Vec2(x, y)

fun Vector3.toVec2() = Vec2(x, y)

operator fun Vector3.plusAssign(other: Vector3) : Unit {
    add(other)
}