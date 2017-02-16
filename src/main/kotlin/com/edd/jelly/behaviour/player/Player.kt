package com.edd.jelly.behaviour.player

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.math.Vector2
import com.edd.jelly.behaviour.components.ComponentResolver
import org.jbox2d.dynamics.contacts.Contact
import org.jbox2d.dynamics.joints.ConstantVolumeJoint
import org.jbox2d.dynamics.joints.WeldJoint

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

    val velocity = Vector2()
    var movingUp = false
    var movingDown = false
    var movingLeft = false
    var movingRight = false
    var sticky = false

    /**
     * How long has the player been off the ground.
     */
    var airTime = 0f

    var movedWithoutTick = 0f
    var health = 100

    var canJump = true

    val stickyJoints = mutableMapOf<Contact, WeldJoint>()

    /**
     * Set of player contacts ground contacts, identified by reference id.
     */
    val groundContacts = mutableSetOf<Contact>()

    /**
     * Set of all player contacts.
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
    var deflationJointLength = 0f

    var speedMultiplier = 1f

    companion object : ComponentResolver<Player>(Player::class.java)
}