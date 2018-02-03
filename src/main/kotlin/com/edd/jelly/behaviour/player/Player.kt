package com.edd.jelly.behaviour.player

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.math.Vector2
import com.edd.jelly.behaviour.physics.jelly.JellyJoint
import com.edd.jelly.util.ComponentResolver
import org.jbox2d.dynamics.contacts.Contact
import org.jbox2d.dynamics.joints.RevoluteJoint

data class Player(
        val joint: JellyJoint,
        val lastSpawn: Vector2
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
    var reset = false

    /**
     * How long has the player been off the ground.
     */
    var airTime = 0f

    var movedWithoutTick = 0f
    var health = 100

    var canJump = true

    val stickyJoints = mutableMapOf<Contact, RevoluteJoint>()

    /**
     * Set of player contacts ground contacts, identified by reference id.
     */
    val groundContacts = mutableSetOf<Contact>()

    /**
     * Set of all player contacts.
     */
    val contacts = mutableSetOf<Contact>()

    /**
     * Was deflation initiated.
     */
    var deflateInitiated = false

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

    /**
     * Check if player has enough ground contacts and sticky joints based on provided contact ratio.
     */
    fun testContactRatio(ratio: Int, additionalContacts: Int = 0): Boolean {
        val limit = joint.bodies.size / ratio
        return (additionalContacts + groundContacts.size) >= limit
                || stickyJoints.size >= limit
    }
}