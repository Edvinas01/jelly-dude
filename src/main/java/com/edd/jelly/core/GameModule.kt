package com.edd.jelly.core

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.edd.jelly.behaviour.*
import com.edd.jelly.behaviour.player.PlayerSystem
import com.edd.jelly.behaviour.rendering.RenderingSystem
import com.edd.jelly.util.Configuration
import com.edd.jelly.util.DebugRenderer
import com.edd.jelly.util.meters
import com.google.inject.Binder
import com.google.inject.Module
import com.google.inject.Provides
import com.google.inject.Singleton
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.World
import org.jbox2d.particle.ParticleSystem

class GameModule(private val game: Game) : Module {

    override fun configure(binder: Binder) {
        binder.requireExactBindingAnnotations()
        binder.requireAtInjectOnConstructors()
    }

    @Provides @Singleton
    fun systems(): Systems {
        return Systems(listOf(

                // Testing.
                TestSystem::class.java,
                CameraControllerSystem::class.java,

                // Physics simulation.
                PhysicsSystem::class.java,

                // Synchronization systems.
                PhysicsSynchronizationSystem::class.java,
                ParticleGroupSynchronizationSystem::class.java,

                RenderingSystem::class.java,
                // Other.
                PlayerSystem::class.java,

                // Rendering.
                PhysicsDebugSystem::class.java
        ))
    }

    @Provides @Singleton
    fun inputRegistrar(): InputMultiplexer {
        return InputMultiplexer()
    }

    @Provides @Singleton
    fun batch(): Batch = SpriteBatch()

    @Provides @Singleton
    fun polygonBatch(): PolygonSpriteBatch = PolygonSpriteBatch()

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
    fun camera(): Camera {
        val width = Gdx.graphics.width.meters
        val height = Gdx.graphics.height.meters

        return OrthographicCamera(width, height).apply {
            position.set(width / 2f, height / 2f, 0f)
            update()
        }
    }

    @Provides @Singleton
    fun engine(): Engine = game.engine
}

data class Systems(val systems: List<Class<out EntitySystem>>)