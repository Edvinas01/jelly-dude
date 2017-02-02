package com.edd.jelly.components

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import org.jbox2d.dynamics.joints.ConstantVolumeJoint

data class PlayerComponent(
        val joint: ConstantVolumeJoint
) : Component {

    var movingUp: Boolean = false
    var movingDown: Boolean = false
    var movingLeft: Boolean = false
    var movingRight: Boolean = false

    companion object : ComponentResolver<PlayerComponent>(PlayerComponent::class.java)
}

val Entity.player: PlayerComponent
    get() = PlayerComponent[this]