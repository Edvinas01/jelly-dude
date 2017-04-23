package com.edd.jelly.behaviour.physics.body

import com.badlogic.ashley.core.Component
import com.edd.jelly.behaviour.components.ComponentResolver
import org.jbox2d.dynamics.Body

data class SoftBody(
        val bodies: List<Body>
) : Component {

    companion object : ComponentResolver<SoftBody>(SoftBody::class.java)
}