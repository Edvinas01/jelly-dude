package com.edd.jelly.core

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.badlogic.gdx.math.EarClippingTriangulator
import com.edd.jelly.behaviour.level.LevelSystem
import com.edd.jelly.behaviour.physics.ParticleGroupSynchronizationSystem
import com.edd.jelly.behaviour.physics.PhysicsDebugSystem
import com.edd.jelly.behaviour.physics.PhysicsSynchronizationSystem
import com.edd.jelly.behaviour.physics.PhysicsSystem
import com.edd.jelly.behaviour.physics.contacts.MessagingContactListener
import com.edd.jelly.behaviour.player.PlayerSynchronizationSystem
import com.edd.jelly.behaviour.player.PlayerSystem
import com.edd.jelly.core.tiled.JellyMapRenderer
import com.edd.jelly.behaviour.rendering.RenderingSystem
import com.edd.jelly.behaviour.test.CameraControllerSystem
import com.edd.jelly.behaviour.test.TestSystem
import com.edd.jelly.core.events.Messaging
import com.edd.jelly.util.Configuration
import com.edd.jelly.behaviour.physics.DebugRenderer
import com.edd.jelly.util.Units
import com.edd.jelly.util.meters
import com.google.inject.Binder
import com.google.inject.Module
import com.google.inject.Provides
import com.google.inject.Singleton
import com.google.inject.name.Named
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.World
import org.jbox2d.particle.ParticleSystem

class GameModule(private val game: Game) : Module {

    companion object {
        const val UI_CAMERA = "uiCamera"
    }

    override fun configure(binder: Binder) {
        binder.requireExactBindingAnnotations()
        binder.requireAtInjectOnConstructors()
    }

    @Provides @Singleton
    fun systems(): Systems {
        return Systems(listOf(
                LevelSystem::class.java,

                // Physics simulation.
                PhysicsSystem::class.java,

                // Testing.
                TestSystem::class.java,
                CameraControllerSystem::class.java,

                // Synchronization systems.
                PhysicsSynchronizationSystem::class.java,
                ParticleGroupSynchronizationSystem::class.java,

                // Player.
                PlayerSystem::class.java,
                PlayerSynchronizationSystem::class.java,

                // Rendering.
                RenderingSystem::class.java,
                PhysicsDebugSystem::class.java
        ))
    }

    @Provides @Singleton
    fun earClippingTriangulator(): EarClippingTriangulator {

        // This object keeps a state, not sure if it can be a singleton.
        return EarClippingTriangulator()
    }

    @Provides @Singleton
    fun inputRegistrar(): InputMultiplexer {
        return InputMultiplexer()
    }

    @Provides @Singleton
    fun polygonBatch(): PolygonSpriteBatch = PolygonSpriteBatch()

    @Provides @Singleton
    fun batch(): SpriteBatch = SpriteBatch()

    @Provides @Singleton
    fun world(): World = World(Vec2(0f, Configuration.GRAVITY)).apply {
        particleRadius = Configuration.PARTICLE_RADIUS
    }

    @Provides @Singleton
    fun debugDraw(): DebugRenderer = DebugRenderer(
            true, true, false, true, false, true
    )

    @Provides @Singleton
    fun particleSystem(world: World): ParticleSystem {
        return ParticleSystem(world)
    }

    @Provides @Singleton
    fun camera(): OrthographicCamera {
        val width = Gdx.graphics.width.meters
        val height = Gdx.graphics.height.meters

        return OrthographicCamera(width, height).apply {
            position.set(width / 2f, height / 2f, 0f)
            update()
        }
    }

    @Provides @Singleton @Named(UI_CAMERA)
    fun uiCamera(): OrthographicCamera {
        val width = Gdx.graphics.width.toFloat()
        val height = Gdx.graphics.height.toFloat()

        return OrthographicCamera(width, height).apply {
            setToOrtho(false, width, height)
        }
    }

    @Provides @Singleton
    fun engine(): Engine = game.engine

    @Provides @Singleton
    fun messaging() = Messaging().pause()

    @Provides @Singleton
    fun messagingContactListener(messaging: Messaging) =
            MessagingContactListener(messaging)

    @Provides @Singleton
    fun tmxMapLoader() = TmxMapLoader()

    @Provides @Singleton
    fun layeredTiledMapRenderer(camera: OrthographicCamera, batch: SpriteBatch) =
            JellyMapRenderer(camera, batch, Units.MPP)
}

data class Systems(val systems: List<Class<out EntitySystem>>)