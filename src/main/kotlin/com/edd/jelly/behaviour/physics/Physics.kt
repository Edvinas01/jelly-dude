package com.edd.jelly.behaviour.physics

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.edd.jelly.behaviour.components.ComponentResolver
import org.jbox2d.dynamics.Body

data class Physics(val body: Body) : Component {

    companion object : ComponentResolver<Physics>(Physics::class.java)
}

val Entity.physics: Physics
    get() = Physics[this]