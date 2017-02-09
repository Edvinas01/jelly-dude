package com.edd.jelly.behaviour.player

import com.badlogic.ashley.core.Component
import com.edd.jelly.behaviour.components.ComponentResolver
import org.jbox2d.dynamics.contacts.Contact
import org.jbox2d.dynamics.joints.ConstantVolumeJoint

data class Player(
        val joint: ConstantVolumeJoint
) : Component {

    var movingUp: Boolean = false
    var movingDown: Boolean = false
    var movingLeft: Boolean = false
    var movingRight: Boolean = false

    /**
     * How long has the player been off the ground.
     */
    var airTime = 0f

    var canJump = true

    /**
     * Set of player contacts, identified by reference id.
     */
    val contacts = mutableSetOf<Contact>()

    companion object : ComponentResolver<Player>(Player::class.java)
}