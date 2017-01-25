package com.edd.jelly.game.components

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.physics.box2d.Body

data class Physics(val body: Body) : Component {

    companion object : ComponentResolver<Physics>(Physics::class.java)
}

val Entity.physics: Physics
    get() = Physics[this]