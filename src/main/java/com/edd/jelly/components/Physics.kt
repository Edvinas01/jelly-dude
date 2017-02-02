package com.edd.jelly.components

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import org.jbox2d.dynamics.Body

data class Physics(val body: Body) : Component {

    companion object : ComponentResolver<Physics>(Physics::class.java)
}

val Entity.physics: Physics
    get() = Physics[this]