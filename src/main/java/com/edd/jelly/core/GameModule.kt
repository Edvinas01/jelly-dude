package com.edd.jelly.core

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer
import com.badlogic.gdx.physics.box2d.World
import com.edd.jelly.systems.*
import com.edd.jelly.util.Configuration
import com.edd.jelly.util.meters
import com.google.inject.Binder
import com.google.inject.Module
import com.google.inject.Provides
import com.google.inject.Singleton

class GameModule(private val game: Game) : Module {

    override fun configure(binder: Binder) {
        binder.requireExactBindingAnnotations()
        binder.requireAtInjectOnConstructors()
    }

    @Provides @Singleton
    fun systems(): Systems {
        return Systems(listOf(
                TestSystem::class.java,
                CameraControllerSystem::class.java,
                PhysicsSystem::class.java,
                PhysicsSynchronizationSystem::class.java,
                RenderingSystem::class.java,
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
    fun world(): World = World(Vector2(0f, Configuration.GRAVITY), true)

    @Provides @Singleton
    fun box2dDebugRenderer(): Box2DDebugRenderer = Box2DDebugRenderer()

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