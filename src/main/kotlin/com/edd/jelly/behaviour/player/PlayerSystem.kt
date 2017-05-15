package com.edd.jelly.behaviour.player

import com.badlogic.ashley.core.*
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.graphics.g2d.PolygonRegion
import com.badlogic.gdx.math.EarClippingTriangulator
import com.badlogic.gdx.math.Vector2
import com.edd.jelly.behaviour.common.event.*
import com.edd.jelly.behaviour.position.Transform
import com.edd.jelly.behaviour.pause.PausingSystem
import com.edd.jelly.behaviour.rendering.PolygonRenderable
import com.edd.jelly.core.events.Messaging
import com.edd.jelly.util.GameException
import com.edd.jelly.util.pixels
import com.edd.jelly.core.resources.ResourceManager
import com.edd.jelly.core.resources.get
import com.edd.jelly.core.scripts.ScriptManager
import com.edd.jelly.behaviour.common.hook.*
import com.edd.jelly.core.configuration.Configurations
import com.edd.jelly.util.take
import com.google.inject.Inject
import org.jbox2d.collision.WorldManifold
import org.jbox2d.collision.shapes.CircleShape
import org.jbox2d.common.MathUtils
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.*
import org.jbox2d.dynamics.contacts.Contact
import org.jbox2d.dynamics.joints.*
import java.util.*

class PlayerSystem @Inject constructor(
        private val earClippingTriangulator: EarClippingTriangulator,
        private val inputMultiplexer: InputMultiplexer,
        private val resourceManager: ResourceManager,
        private val messaging: Messaging,
        private val random: Random,
        private val world: World,
        configurations: Configurations,
        scriptManager: ScriptManager
) : EntitySystem(), PausingSystem {

    private companion object {

        // Player body constants.
        val PLAYER_TEXTURE_NAME = "slime"
        val PLAYER_VERTEX_COUNT = 40
        val PLAYER_VERTEX_SIZE = 0.02f
        val PLAYER_WIDTH = 1.1f
        val PLAYER_HEIGHT = 1.2f
        val SIZE_LOSS = 0.995f
        val DAMPING = 2f
        val DENSITY = 1f
        val HZ = 20f

        /**
         * The force which is applied to the player when its moving.
         */
        val MOVE_FORCE = 3f * DENSITY
        val MAX_VELOCITY = 7f

        /**
         * Player air time in seconds.
         */
        val MAX_AIR_TIME = 0.2f

        /**
         * Minimum player physics contact ratio for when it counts that a player is touching the ground. The higher the
         * number, the less contacts are needed.
         */
        val MIN_CONTACT_RATIO = 5

        /**
         * How much does the player have to travel in order for a health tick to occur.
         */
        val HEALTH_TICK_DISTANCE = 30
        val MOVEMENT_TICK_MULTIPLIER = 20

        // Deflation stuff.
        val DEFLATION_SPEED_MULTIPLIER = 0.5f
        val DEFLATION_JOINT_LENGTH = 0.04f
        val DEFLATION_AMOUNT = 0.5f
        val DEFLATION_SPEED = 7

        // Player sounds.
        val SOUND_NAMES = (1..5).map { "slime_$it" }
        val SOUND_VOLUME_MULTIPLIER = 0.2f
        val SOUND_LOW_PITCH = 0.6f
    }

    private val beforeMoveHook = scriptManager.hook(BeforeMove::class.java)
    private val afterMoveHook = scriptManager.hook(AfterMove::class.java)
    private val moveForceHook = scriptManager.hook(MoveForce::class.java)
    private val healthHook = scriptManager.hook(BeforeHealthTick::class.java)
    private val stickHook = scriptManager.hook(BeforeStick::class.java)

    private val playerInputs = PlayerInputAdapter(messaging).apply {
        adaptInputs(configurations.config.input)
    }

    override fun addedToEngine(engine: Engine) {
        super.addedToEngine(engine)

        inputMultiplexer.addProcessor(playerInputs)

        // Initialize player event listeners.
        initListeners()
    }

    override fun update(deltaTime: Float) {
        engine.getEntitiesFor(Family.all(
                Transform::class.java,
                Player::class.java).get()
        ).forEach { entity ->

            // At the moment only one player is allowed.
            val player = Player.mapper[entity]

            processMovement(player, deltaTime)
            processHealth(player, entity, deltaTime)
            processStickiness(player)
            processDeflation(player, deltaTime)
        }
    }

    /**
     * Process player movement and jumping.
     */
    private fun processMovement(player: Player, deltaTime: Float) {
        with(player) {
            beforeMoveHook.run {
                it.beforeMove(this)
            }

            // If player has no contacts or joints at the moment, it means he is in the air.
            if (groundContacts.isEmpty() && stickyJoints.isEmpty()) {
                airTime += deltaTime
            }

            // Count particle contacts with the player.
            val particleContacts = (0 until world.particleBodyContactCount)
                    .map { world.particleBodyContacts[it] }
                    .count { it.body.userData == this }

            // Player can jump if hes been in air for not too long or has some sticky joints.
            canJump = airTime < MAX_AIR_TIME || testContactRatio(MIN_CONTACT_RATIO, particleContacts)

            // Calculated move force for this player.
            var moveForce = speedMultiplier * MOVE_FORCE * deltaTime

            moveForceHook.run {
                moveForce = it.moveForce(moveForce)
            }

            for (body in joint.bodies) {

                // Current velocity of this body.
                val velocity = body.linearVelocity

                // Up and down movement.
                if (canJump && movingUp && velocity.y < MAX_VELOCITY) {
                    body.applyForceToCenter(Vec2(0f, moveForce * 4))
                }
                if (movingDown && velocity.y > -MAX_VELOCITY) {
                    body.applyForceToCenter(Vec2(0f, -moveForce))
                }

                // Left and right movement.
                if (movingLeft && velocity.x > -MAX_VELOCITY) {
                    body.applyForceToCenter(Vec2(-moveForce, 0f))
                }
                if (movingRight && velocity.x < MAX_VELOCITY) {
                    body.applyForceToCenter(Vec2(moveForce, 0f))
                }
            }

            afterMoveHook.run {
                it.afterMove(this)
            }
        }
    }

    /**
     * Process player health drain.
     */
    private fun processHealth(player: Player, entity: Entity, deltaTime: Float) {
        with(player) {
            if (contacts.isEmpty()) {
                return
            }

            if (movedWithoutTick > HEALTH_TICK_DISTANCE) {

                var doTick = true
                healthHook.run {
                    doTick = it.beforeHealthTick(player, movedWithoutTick)
                }

                if (doTick) {
                    movedWithoutTick = 0f
                    joint.inflate(SIZE_LOSS)
                    health--
                }
            }
            movedWithoutTick += (Math.abs(velocity.x) + Math.abs(velocity.y)) * deltaTime * MOVEMENT_TICK_MULTIPLIER

            if (health <= 0) {
                engine.removeEntity(entity)
            }
        }
    }

    /**
     * Process player stickiness to objects.
     */
    private fun processStickiness(player: Player) {
        with(player) {
            if (sticky) {

                var actuallySticky = true
                stickHook.run {
                    actuallySticky = it.beforeStick(player)
                }

                if (!actuallySticky) {
                    return
                }

                @Suppress("LoopToCallChain")
                for (contact in contacts) {
                    if (stickyJoints.containsKey(contact)

                            // Joint's don't seem to work with static bodies.
                            || BodyType.STATIC == contact.fixtureA.body.type
                            || BodyType.STATIC == contact.fixtureB.body.type) {

                        continue
                    }

                    // Create a joint to where a player is contacting another body.
                    stickyJoints.put(contact, world.createJoint(RevoluteJointDef().apply {
                        val manifold = WorldManifold()
                        contact.getWorldManifold(manifold)

                        initialize(contact.fixtureA.body, contact.fixtureB.body, manifold.points.first())

                        collideConnected = true
                    }) as RevoluteJoint)
                }
            } else {

                // Player is not sticky, cleanup old joints.
                stickyJoints.forEach {
                    world.destroyJoint(it.value)
                }
                stickyJoints.clear()
            }
        }
    }

    /**
     * Process shrinking and growing of a player.
     */
    private fun processDeflation(player: Player, deltaTime: Float) {
        with(player) {

            // Key to deflate or inflate was pressed or released.
            if (deflationState == Player.Deflation.DEFLATE || deflationState == Player.Deflation.INFLATE) {
                val direction = if (deflationState == Player.Deflation.DEFLATE) {
                    deflationJointLength = -DEFLATION_JOINT_LENGTH
                    speedMultiplier = DEFLATION_SPEED_MULTIPLIER
                    1f
                } else {
                    deflationJointLength = DEFLATION_JOINT_LENGTH
                    speedMultiplier = 1f
                    -1f
                }

                // How much should deflate.
                deflation = direction * DEFLATION_AMOUNT - direction * deflation.take(DEFLATION_AMOUNT)

                // How much should the joints be strengthened on deflation.
                deflationState = Player.Deflation.WORKING
            }

            if (deflationState == Player.Deflation.WORKING) {
                val sign = Math.signum(deflation)
                if (Math.abs(deflation) > 0) {

                    // How much should we deflate this tick.
                    val dt = deflation.take(
                            -sign * DEFLATION_SPEED

                    ) * deltaTime * DEFLATION_SPEED

                    joint.targetVolume -= dt
                    deflation += dt
                } else {
                    deflationState = Player.Deflation.IDLE
                }
            }
        }
    }

    /**
     * Create polygon region from provided transform position, size and body vertices.
     */
    private fun createPlayerTexture(transform: Transform, bodies: Array<Body>): PolygonRegion {
        val playerTexture = resourceManager.atlas[PLAYER_TEXTURE_NAME]!!

        val xRatio = playerTexture.regionWidth / transform.width.pixels
        val yRatio = playerTexture.regionHeight / transform.height.pixels

        val halfWidth = playerTexture.regionWidth / 2
        val halfHeight = playerTexture.regionHeight / 2

        val worldPoint = Vec2(transform.position.x, transform.position.y)
        val vertices = FloatArray(bodies.size * 2)

        bodies.forEachIndexed { i, body ->

            // x coordinates.
            vertices[i * 2] =
                    -body.getLocalPoint(worldPoint).x.pixels * xRatio + halfWidth

            // y coordinates.
            vertices[i * 2 + 1] =
                    -body.getLocalPoint(worldPoint).y.pixels * yRatio + halfHeight
        }
        return PolygonRegion(
                playerTexture,
                vertices,
                earClippingTriangulator.computeTriangles(vertices).toArray())
    }

    /**
     * Spawn player at a given location.
     */
    private fun spawnPlayer(x: Float,
                            y: Float,
                            width: Float = PLAYER_WIDTH,
                            height: Float = PLAYER_HEIGHT): Entity {

        val players = engine.getEntitiesFor(Family
                .all(Player::class.java)
                .get())

        // For now only one player is allowed.
        if (players.size() > 0) {
            throw GameException("Only one player can exist")
        }

        val entity = Entity().apply {
            val joint = world.createJoint(ConstantVolumeJointDef().apply {
                collideConnected = false
                dampingRatio = DAMPING
                frequencyHz = HZ

                // We're going round in a circle, radius will be double, so have to divide by two.
                val halfWidth = width / 2
                val halfHeight = height / 2

                for (vertex in 0..PLAYER_VERTEX_COUNT - 1) {

                    // Should add bodies as entities to the engine?
                    addBody(world.createBody(BodyDef().apply {
                        val angle = MathUtils.map(
                                vertex.toFloat(),
                                0f,
                                PLAYER_VERTEX_COUNT.toFloat(),
                                0f,
                                2 * MathUtils.PI)

                        fixedRotation = true
                        position = Vec2(
                                x + halfWidth * MathUtils.sin(angle),
                                y + halfHeight * MathUtils.cos(angle))

                        type = BodyType.DYNAMIC

                    }).apply {
                        createFixture(FixtureDef().apply {
                            density = DENSITY
                            shape = CircleShape().apply {
                                radius = PLAYER_VERTEX_SIZE
                            }
                        })
                    })
                }
            }) as ConstantVolumeJoint

            val transform = Transform(
                    Vector2(x, y),
                    Vector2(width, height))

            val player = Player(joint, Vector2(x, y))
            joint.bodies.forEach { body ->
                body.userData = player
            }

            // Register player entity components.
            add(PolygonRenderable(createPlayerTexture(transform, joint.bodies)))
            add(player)
            add(transform)

        }

        engine.addEntity(entity)
        playerInputs.player = entity
        return entity
    }

    /**
     * Destroy player entity.
     */
    private fun destroyPlayer(player: Entity): Player {
        with(Player.mapper[player]) {
            groundContacts.clear()
            contacts.clear()

            for ((_, value) in stickyJoints) {
                world.destroyJoint(value)
            }
            stickyJoints.clear()

            world.destroyJoint(joint)
            for (body in joint.bodies) {
                world.destroyBody(body)
            }

            playerInputs.player = null
            return this
        }
    }

    /**
     * Initialize physics and Ashley listeners for player system.
     */
    private fun initListeners() {

        // Listen for player destruction.
        engine.addEntityListener(Family.one(Player::class.java).get(), object : EntityListener {
            override fun entityRemoved(entity: Entity) {
                val player = destroyPlayer(entity)

                if (player.reset) {
                    with(player.lastSpawn) {
                        spawnPlayer(x, y)
                    }
                }
            }

            override fun entityAdded(entity: Entity) {
            }
        })

        // Listen for when player stops touching an object.
        messaging.listen<EndContactEvent> { (contact) ->
            resolveContact(contact)?.let {
                with(it.first) {

                    // Cleanup the contacts.
                    groundContacts.remove(contact)
                    contacts.remove(contact)
                }
            }
        }

        // Listen for when player starts touching an object.
        messaging.listen<BeginContactEvent> { (contact) ->

            // Did a player trigger this contact?
            resolveContact(contact)?.let {

                // Play sound if just touched the ground or something else.
                val player = it.first
                if (player.contacts.isEmpty()) {
                    messaging.send(PlaySoundEvent(
                            name = SOUND_NAMES[random.nextInt(SOUND_NAMES.size - 1)],
                            volumeMultiplier = SOUND_VOLUME_MULTIPLIER,
                            lowPitch = SOUND_LOW_PITCH
                    ))
                }

                with(contact) {

                    // Always populate the list of all contacts.
                    it.first.contacts.add(this)

                    val manifold = WorldManifold()
                    getWorldManifold(manifold)

                    // Player hit the upper part of the body.
                    if (manifold.normal.y > 0) {
                        with(it.first) {
                            groundContacts.add(contact)

                            // Enough contacts registered to reset player air time.
                            if (testContactRatio(MIN_CONTACT_RATIO)) {
                                airTime = 0f
                            }
                        }
                    }
                }
            }
        }

        // Listen for player inputs.
        messaging.listen<PlayerInputEvent> { (player, reset) ->
            if (reset) {
                engine.removeEntity(player)
            }
        }

        // Listen for level loads.
        messaging.listen<LevelLoadedEvent> { (map) ->
            map.spawn?.let {
                spawnPlayer(it.x, it.y)
            }
        }

        messaging.listen<ConfigChangedEvent> {
            playerInputs.adaptInputs(it.config.input)
        }
    }

    /**
     * Resolve player and body which player is touching from a contact.
     */
    private fun resolveContact(contact: Contact): Pair<Player, Body>? {
        val dataA = contact.fixtureA.body.userData
        val dataB = contact.fixtureB.body.userData

        // Player is touching himself, ignore.
        if (dataA is Player && dataB is Player) {
            return null
        }

        if (dataA is Player) {
            return Pair(dataA, contact.fixtureB.body)
        }
        if (dataB is Player) {
            return Pair(dataB, contact.fixtureA.body)
        }
        return null
    }

    override fun paused(pause: Boolean) {
        playerInputs.disabled = pause
    }
}