package com.edd.jelly

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.*
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType
import finnstr.libgdx.liquidfun.ParticleDebugRenderer
import finnstr.libgdx.liquidfun.ParticleDef.ParticleType
import finnstr.libgdx.liquidfun.ParticleGroupDef
import finnstr.libgdx.liquidfun.ParticleSystem
import finnstr.libgdx.liquidfun.ParticleSystemDef

class LiquidFunTest : ApplicationAdapter(), InputProcessor {

    private var camera: OrthographicCamera? = null
    private var batch: SpriteBatch? = null

    private var mWorld: World? = null
    private var mParticleSystem: ParticleSystem? = null
    private var mParticleDebugRenderer: ParticleDebugRenderer? = null
    private var mDebugRenderer: Box2DDebugRenderer? = null

    private var mParticleGroupDef1: ParticleGroupDef? = null
    private var mParticleGroupDef2: ParticleGroupDef? = null

    override fun create() {
        val width = Gdx.graphics.width.toFloat()
        val height = Gdx.graphics.height.toFloat()

        camera = OrthographicCamera(width, height)
        camera!!.position.set(width / 2, height / 2, 0f)
        camera!!.update()
        Gdx.input.inputProcessor = this

        batch = SpriteBatch()

        createBox2DWorld(width, height)
        createParticleStuff(width, height)

        /* Render stuff */
        mDebugRenderer = Box2DDebugRenderer()
        mParticleDebugRenderer = ParticleDebugRenderer(Color(0f, 1f, 0f, 1f), mParticleSystem!!.particleCount)

        /* Version */
        Gdx.app.log("Running LiquidFun version", mParticleSystem!!.versionString)
        updateLog()
    }

    private fun createBox2DWorld(width: Float, height: Float) {
        mWorld = World(Vector2(0f, -9.8f), false)

        /* Bottom */
        val bodyDef = BodyDef()
        bodyDef.type = BodyType.StaticBody
        bodyDef.position.set(width * WORLD_TO_BOX / 2f, height * (2f / 100f) * WORLD_TO_BOX / 2f)
        //bodyDef.angle = (float) Math.toRadians(-30);
        val ground = mWorld!!.createBody(bodyDef)

        var shape = PolygonShape()
        shape.setAsBox(width * WORLD_TO_BOX / 2, height * (2f / 100f) * WORLD_TO_BOX / 2f)

        val fixDef = FixtureDef()
        fixDef.friction = 0.2f
        fixDef.shape = shape
        ground.createFixture(fixDef)

        shape.dispose()

        /* Walls */
        val bodyDef1 = BodyDef()
        bodyDef1.type = BodyType.StaticBody
        bodyDef1.position.set(width * (2f / 100f) * WORLD_TO_BOX / 2f, height * WORLD_TO_BOX / 2)
        val left = mWorld!!.createBody(bodyDef1)

        bodyDef1.position.set(width * WORLD_TO_BOX - width * (2f / 100f) * WORLD_TO_BOX / 2f, height * WORLD_TO_BOX / 2)
        val right = mWorld!!.createBody(bodyDef1)

        shape = PolygonShape()
        shape.setAsBox(width * (2f / 100f) * WORLD_TO_BOX / 2f, height * WORLD_TO_BOX / 2)
        fixDef.shape = shape

        left.createFixture(fixDef)
        right.createFixture(fixDef)
        shape.dispose()
    }

    private fun createParticleStuff(width: Float, height: Float) {
        //First we create a new particlesystem and
        //set the radius of each particle to 6 / 120 m (5 cm)
        val systemDef = ParticleSystemDef()
        systemDef.radius = 6f * WORLD_TO_BOX
        systemDef.dampingStrength = 0.2f

        mParticleSystem = ParticleSystem(mWorld!!, systemDef)
        mParticleSystem!!.particleDensity = 1.3f

        //Create a new particlegroupdefinition and set some properties
        //For the flags you can set more than only one
        mParticleGroupDef1 = ParticleGroupDef()
        mParticleGroupDef1!!.color.set(1f, 0f, 0f, 1f)
        mParticleGroupDef1!!.flags.add(ParticleType.b2_waterParticle)
        mParticleGroupDef1!!.position.set(width * (30f / 100f) * WORLD_TO_BOX, height * (80f / 100f) * WORLD_TO_BOX)

        //Create a shape, give it to the definition and
        //create the particlegroup in the particlesystem.
        //This will return you a ParticleGroup instance, but
        //we don't need it here, so we drop that.
        //The shape defines where the particles are created exactly
        //and how much are created
        val parShape = PolygonShape()
        parShape.setAsBox(width * (20f / 100f) * WORLD_TO_BOX / 2f, width * (20f / 100f) * WORLD_TO_BOX / 2f)
        mParticleGroupDef1!!.shape = parShape
        mParticleSystem!!.createParticleGroup(mParticleGroupDef1!!)

        //Exactly the same! This is the second group with a different
        //color and shifted on the x-Axis
        mParticleGroupDef2 = ParticleGroupDef()
        mParticleGroupDef2!!.shape = mParticleGroupDef1!!.shape
        mParticleGroupDef2!!.flags = mParticleGroupDef1!!.flags
        mParticleGroupDef2!!.groupFlags = mParticleGroupDef1!!.groupFlags
        mParticleGroupDef2!!.position.set(width * (70f / 100f) * WORLD_TO_BOX, height * (80f / 100f) * WORLD_TO_BOX)
        mParticleGroupDef2!!.color.set(0.2f, 1f, 0.3f, 1f)
        mParticleSystem!!.createParticleGroup(mParticleGroupDef2!!)

        //Here we create a new shape and we set a
        //linear velocity. This is used in createParticles1()
        //and createParticles2()
        val partShape = CircleShape()
        partShape.radius = 18.5f * WORLD_TO_BOX

        mParticleGroupDef1!!.shape = partShape
        mParticleGroupDef2!!.shape = partShape

        mParticleGroupDef1!!.linearVelocity.set(Vector2(0f, -10f))
        mParticleGroupDef2!!.linearVelocity.set(Vector2(0f, -10f))
    }

    override fun render() {
        //First update our InputProcessor
        this.inputUpdate(Gdx.graphics.deltaTime)

        //Now do the same as every year...
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        mWorld!!.step(Gdx.graphics.deltaTime, 10, 6, mParticleSystem!!.calculateReasonableParticleIterations(Gdx.graphics.deltaTime))

        batch!!.projectionMatrix = camera!!.combined
        batch!!.begin()
        batch!!.end()

        //Get the combined matrix and scale it down to
        //our Box2D size
        val cameraCombined = camera!!.combined.cpy()
        cameraCombined.scale(BOX_TO_WORLD, BOX_TO_WORLD, 1f)

        //First render the particles and then the Box2D world
        mParticleDebugRenderer!!.render(mParticleSystem!!, BOX_TO_WORLD, cameraCombined)
        mDebugRenderer!!.render(mWorld, cameraCombined)
    }

    override fun dispose() {
        batch!!.dispose()
        mParticleGroupDef1!!.shape.dispose()
        mWorld!!.dispose()
        mDebugRenderer!!.dispose()
    }

    fun createParticles1(pX: Float, pY: Float) {
        mParticleGroupDef1!!.position.set(pX * WORLD_TO_BOX, pY * WORLD_TO_BOX)
        mParticleSystem!!.createParticleGroup(mParticleGroupDef1!!)
        updateParticleCount()
        updateLog()
    }

    private fun createParticles2(pX: Float, pY: Float) {
        mParticleGroupDef2!!.position.set(pX * WORLD_TO_BOX, pY * WORLD_TO_BOX)
        mParticleSystem!!.createParticleGroup(mParticleGroupDef2!!)
        updateParticleCount()
        updateLog()
    }

    private fun updateParticleCount() {
        if (mParticleSystem!!.particleCount > mParticleDebugRenderer!!.maxParticleNumber) {
            mParticleDebugRenderer!!.maxParticleNumber = mParticleSystem!!.particleCount + 1000
        }
    }

    fun updateLog() {
        //Here we log the total particle count and the f/s
        Gdx.app.log("", "Total particles: " + mParticleSystem!!.particleCount + " FPS: " + Gdx.graphics.framesPerSecond)
    }

    fun createCircleBody(pX: Float, pY: Float, pRadius: Float) {
        val bodyDef = BodyDef()
        bodyDef.type = BodyType.DynamicBody
        bodyDef.position.set(pX * WORLD_TO_BOX, pY * WORLD_TO_BOX)
        val body = mWorld!!.createBody(bodyDef)

        val shape = CircleShape()
        shape.radius = pRadius * WORLD_TO_BOX

        val fixDef = FixtureDef()
        fixDef.density = 0.5f
        fixDef.friction = 0.2f
        fixDef.shape = shape
        fixDef.restitution = 0.3f

        body.createFixture(fixDef)
    }

    /* +++ Input +++ */

    private val CREATE_PARTICLE_FREQUENCY = 50f
    private var mTotDelta = 0f

    private var mCreateParticles = false
    private var mPointerPosX = 0f
    private var mPointerPosY = 0f
    private var mCurrentButton = -1

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        if (button != Input.Buttons.LEFT && button != Input.Buttons.RIGHT && button != Input.Buttons.MIDDLE) return false

        if (button == Input.Buttons.MIDDLE) {
            this.createCircleBody(screenX.toFloat(), (Gdx.graphics.height - screenY).toFloat(), MathUtils.random(10, 80).toFloat())
            return true
        }

        mCreateParticles = true
        mCurrentButton = button
        mTotDelta = 0f

        mPointerPosX = screenX.toFloat()
        mPointerPosY = screenY.toFloat()

        return true
    }

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        if (button != Input.Buttons.LEFT && button != Input.Buttons.RIGHT && button != Input.Buttons.MIDDLE) return false

        mCreateParticles = false
        return true
    }

    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
        if (!mCreateParticles) return false

        mPointerPosX = screenX.toFloat()
        mPointerPosY = screenY.toFloat()

        return true
    }

    override fun mouseMoved(screenX: Int, screenY: Int): Boolean {
        return false
    }

    override fun scrolled(amount: Int): Boolean {
        return false
    }

    fun inputUpdate(pDelta: Float) {
        if (!mCreateParticles) return
        mTotDelta += pDelta

        if (mTotDelta >= 1f / CREATE_PARTICLE_FREQUENCY) {
            mTotDelta -= 1 / CREATE_PARTICLE_FREQUENCY
        } else
            return

        val x = mPointerPosX + MathUtils.random(-Gdx.graphics.width * (1.5f / 100f), Gdx.graphics.width * (1.5f / 100f))
        val y = mPointerPosY + MathUtils.random(-Gdx.graphics.height * (1.5f / 100f), Gdx.graphics.height * (1.5f / 100f))

        if (mCurrentButton == Input.Buttons.LEFT) {
            this.createParticles1(x, Gdx.graphics.height - y)
        } else if (mCurrentButton == Input.Buttons.RIGHT) {
            this.createParticles2(x, Gdx.graphics.height - y)
        }
    }

    override fun keyDown(keycode: Int): Boolean {
        return false
    }

    override fun keyUp(keycode: Int): Boolean {
        return false
    }

    override fun keyTyped(character: Char): Boolean {
        return false
    }

    companion object {

        private val BOX_TO_WORLD = 120.0f
        private val WORLD_TO_BOX = 1f / BOX_TO_WORLD
    }

}