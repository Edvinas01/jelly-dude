package com.edd.jelly.behaviour.physics.body

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.edd.jelly.util.ComponentResolver
import org.jbox2d.dynamics.Body

data class RigidBody(val body: Body) : Component {

    companion object : ComponentResolver<RigidBody>(RigidBody::class.java)
}

val Entity.rigidBody: RigidBody
    get() = RigidBody[this]