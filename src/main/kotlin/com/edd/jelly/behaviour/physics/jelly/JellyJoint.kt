package com.edd.jelly.behaviour.physics.jelly

import org.jbox2d.common.MathUtils
import org.jbox2d.common.Settings
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.Body
import org.jbox2d.dynamics.SolverData
import org.jbox2d.dynamics.World
import org.jbox2d.dynamics.joints.DistanceJoint
import org.jbox2d.dynamics.joints.DistanceJointDef
import org.jbox2d.dynamics.joints.Joint

class JellyJoint(private val world: World, def: JellyJointDef) : Joint(world.pool, def) {

    val bodies: Array<Body>

    var targetVolume = 0f

    private val distanceJoints: Array<DistanceJoint>
    private val normals: Array<Vec2>

    private var impulse = 0.0f

    init {
        if (def.bodies.size < 3) {
            throw IllegalArgumentException(
                    "Joint must contain at least three bodies."
            )
        }

        bodies = def.bodies.toTypedArray()
        normals = Array(bodies.size, { Vec2() })

        targetVolume = calculateArea { current, next ->
            bodies[current].worldCenter.to(bodies[next].worldCenter)
        }

        // Initialize target lengths for each jelly edge.
        val targetLengths = FloatArray(bodies.size)
        for (i in targetLengths.indices) {
            val next = if (i == targetLengths.size - 1) {
                0
            } else {
                i + 1
            }

            val currentCenter = bodies[i].worldCenter
            val nextCenter = bodies[next].worldCenter

            targetLengths[i] = currentCenter.sub(nextCenter).length()
        }

        // Initialize jelly edges.
        val jointDef = DistanceJointDef()
        distanceJoints = Array(targetLengths.size, { i ->
            val next = if (i == targetLengths.size - 1) {
                0
            } else {
                i + 1
            }

            jointDef.frequencyHz = def.frequencyHz
            jointDef.dampingRatio = def.dampingRatio
            jointDef.collideConnected = def.collideConnected

            val currentBody = bodies[i]
            val nextBody = bodies[next]

            jointDef.initialize(
                    currentBody,
                    nextBody,
                    currentBody.worldCenter,
                    nextBody.worldCenter
            )

            world.createJoint(jointDef) as DistanceJoint
        })
    }

    fun inflate(factor: Float) {
        targetVolume *= factor
    }

    override fun destructor() {
        for (distanceJoint in distanceJoints) {
            world.destroyJoint(distanceJoint)
        }
    }

    override fun initVelocityConstraints(step: SolverData) {
        val velocities = step.velocities
        val positions = step.positions
        val distances = pool.getVec2Array(bodies.size)

        for (i in bodies.indices) {
            val prev = if (i == 0) {
                bodies.size - 1
            } else {
                i - 1
            }

            val next = if (i == bodies.size - 1) {
                0
            } else {
                i + 1
            }

            distances[i].apply {
                set(positions[bodies[next].m_islandIndex].c)
                subLocal(positions[bodies[prev].m_islandIndex].c)
            }
        }

        if (step.step.warmStarting) {
            impulse *= step.step.dtRatio

            for (i in bodies.indices) {
                val body = bodies[i]
                val distance = distances[i]

                velocities[body.m_islandIndex].v.apply {
                    x += body.m_invMass * distance.y * .5f * impulse
                    y += body.m_invMass * -distance.x * .5f * impulse
                }
            }
        } else {
            impulse = 0.0f
        }
    }

    override fun solvePositionConstraints(step: SolverData): Boolean {
        val positions = step.positions

        var perimeter = 0.0f
        for (i in bodies.indices) {
            val next = if (i == bodies.size - 1) {
                0
            } else {
                i + 1
            }

            val currentCenter = positions[bodies[i].m_islandIndex].c
            val nextCenter = positions[bodies[next].m_islandIndex].c

            val dx = nextCenter.x - currentCenter.x
            val dy = nextCenter.y - currentCenter.y

            var distance = MathUtils.sqrt(dx * dx + dy * dy)
            if (distance < Settings.EPSILON) {
                distance = 1.0f
            }

            normals[i].apply {
                x = dy / distance
                y = -dx / distance
            }

            perimeter += distance
        }

        val deltaArea = targetVolume - calculateArea { current, next ->
            positions[bodies[current].m_islandIndex].c.to(positions[bodies[next].m_islandIndex].c)
        }

        val delta = pool.popVec2()
        val toExtrude = 0.5f * deltaArea / perimeter

        var done = true
        for (i in bodies.indices) {
            val next = if (i == bodies.size - 1) {
                0
            } else {
                i + 1
            }

            val currentNormal = normals[i]
            val nextNormal = normals[next]

            delta.set(toExtrude * (currentNormal.x + nextNormal.x), toExtrude * (currentNormal.y + nextNormal.y))

            val normalSquared = delta.lengthSquared()
            if (normalSquared > Settings.maxLinearCorrection * Settings.maxLinearCorrection) {
                delta.mulLocal(Settings.maxLinearCorrection / MathUtils.sqrt(normalSquared))
            }
            if (normalSquared > Settings.linearSlop * Settings.linearSlop) {
                done = false
            }

            positions[bodies[next].m_islandIndex].c.apply {
                x += delta.x
                y += delta.y
            }
        }

        pool.pushVec2(1)
        return done
    }

    override fun solveVelocityConstraints(step: SolverData) {
        var crossMassSum = 0.0f
        var dotMassSum = 0.0f

        val velocities = step.velocities
        val positions = step.positions
        val distances = pool.getVec2Array(bodies.size)

        for (i in bodies.indices) {
            val prev = if (i == 0) {
                bodies.size - 1
            } else {
                i - 1
            }

            val next = if (i == bodies.size - 1) {
                0
            } else {
                i + 1
            }

            val distance = distances[i]
            distance.set(positions[bodies[next].m_islandIndex].c)
            distance.subLocal(positions[bodies[prev].m_islandIndex].c)

            val body = bodies[i]
            dotMassSum += distance.lengthSquared() / body.mass
            crossMassSum += Vec2.cross(velocities[body.m_islandIndex].v, distance)
        }

        val lambda = -2.0f * crossMassSum / dotMassSum

        impulse += lambda
        for (i in bodies.indices) {
            val distance = distances[i]
            val body = bodies[i]

            velocities[body.m_islandIndex].v.apply {
                x += body.m_invMass * distance.y * .5f * lambda
                y += body.m_invMass * -distance.x * .5f * lambda
            }
        }
    }

    override fun getAnchorA(out: Vec2) {
    }

    override fun getAnchorB(out: Vec2) {
    }

    override fun getReactionForce(invDt: Float, out: Vec2) {
    }

    override fun getReactionTorque(invDt: Float) = 0f

    /**
     * Calculate body area given a function which returns a pair of currently iterated and the next bodies center.
     */
    private fun calculateArea(centerAccessor: (current: Int, next: Int) -> Pair<Vec2, Vec2>): Float {
        var area = 0.0f
        for (i in bodies.indices) {
            val next = if (i == bodies.size - 1) {
                0
            } else {
                i + 1
            }

            centerAccessor(i, next).also { (currentCenter, nextCenter) ->
                area += currentCenter.x * nextCenter.y - nextCenter.x * currentCenter.y
            }
        }
        return area / 2
    }
}