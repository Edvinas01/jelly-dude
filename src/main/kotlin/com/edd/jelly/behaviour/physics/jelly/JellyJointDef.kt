package com.edd.jelly.behaviour.physics.jelly

import org.jbox2d.dynamics.Body
import org.jbox2d.dynamics.World
import org.jbox2d.dynamics.joints.JointDef
import org.jbox2d.dynamics.joints.JointType

class JellyJointDef : JointDef(JointType.UNKNOWN) {

    var frequencyHz = 0f
    var dampingRatio = 0f

    val bodies = mutableListOf<Body>()

    init {
        collideConnected = false
        frequencyHz = 0.0f
        dampingRatio = 0.0f
    }

    /**
     * Adds a body to the group.
     */
    fun addBody(body: Body) {
        bodies.add(body)
        if (bodies.size == 1) {
            bodyA = body
        }
        if (bodies.size == 2) {
            bodyB = body
        }
    }

    override fun create(world: World) = JellyJoint(world, this)
}