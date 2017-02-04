package com.edd.jelly.behaviour.player

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.edd.jelly.behaviour.components.ComponentResolver
import org.jbox2d.dynamics.joints.ConstantVolumeJoint

data class Player(
        val joint: ConstantVolumeJoint
) : Component {

    var movingUp: Boolean = false
    var movingDown: Boolean = false
    var movingLeft: Boolean = false
    var movingRight: Boolean = false

    companion object : ComponentResolver<Player>(Player::class.java)
}

val Entity.player: Player
    get() = Player[this]