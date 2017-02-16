package com.edd.jelly.behaviour.player

import com.badlogic.ashley.core.Component
import com.edd.jelly.behaviour.components.ComponentResolver
import org.jbox2d.dynamics.contacts.Contact
import org.jbox2d.dynamics.joints.ConstantVolumeJoint

data class Player(
        val joint: ConstantVolumeJoint
) : Component {

    /**
     * Deflation states.
     */
    enum class Deflation {
        IDLE,
        WORKING,
        DEFLATE,
        INFLATE
    }

    var movingUp = false
    var movingDown = false
    var movingLeft = false
    var movingRight = false
    var stick = false

    /**
     * How long has the player been off the ground.
     */
    var airTime = 0f
    var canJump = true

    /**
     * Set of player contacts, identified by reference id.
     */
    val contacts = mutableSetOf<Contact>()

    /**
     * How much should the player deflate.
     */
    var deflation = 0f

    /**
     * Current deflation state.
     */
    var deflationState = Deflation.IDLE
    var deflationJointMultiplier = 1f

    var speedMultiplier = 1f

    companion object : ComponentResolver<Player>(Player::class.java)
}