package com.edd.jelly.core

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.badlogic.gdx.math.EarClippingTriangulator
import com.edd.jelly.behaviour.level.LevelSystem
import com.edd.jelly.behaviour.physics.*
import com.edd.jelly.behaviour.physics.contacts.MessagingContactListener
import com.edd.jelly.behaviour.player.PlayerSynchronizationSystem
import com.edd.jelly.behaviour.player.PlayerSystem
import com.edd.jelly.behaviour.rendering.RenderingSystem
import com.edd.jelly.behaviour.test.CameraControllerSystem
import com.edd.jelly.behaviour.test.TestSystem
import com.edd.jelly.behaviour.ui.UISystem
import com.edd.jelly.core.configuration.Configurations
import com.edd.jelly.core.events.Messaging
import com.edd.jelly.core.scripts.ScriptManager
import com.edd.jelly.core.tiled.JellyMapRenderer
import com.edd.jelly.debug.DebugSystem
import com.edd.jelly.util.Units
import com.edd.jelly.util.meters
import com.google.inject.*
import jdk.nashorn.api.scripting.NashornScriptEngine
import org.apache.commons.io.monitor.FileAlterationMonitor
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.World
import org.jbox2d.particle.ParticleSystem
import javax.script.ScriptEngineManager

class GameModule(private val game: JellyGame) : Module {

    override fun configure(binder: Binder) {
        binder.requireExactBindingAnnotations()
        binder.requireAtInjectOnConstructors()

        binder.bind(Configurations::class.java)
                .toInstance(game.configurations)

        binder.bind(Messaging::class.java)
                .toInstance(game.messaging)
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
                PhysicsDebugSystem::class.java,
                DebugSystem::class.java,
                UISystem::class.java
        ))
    }

    @Provides @Singleton
    fun game() = game

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
    fun shapeRenderer(): ShapeRenderer = ShapeRenderer()

    @Provides @Singleton
    fun world(configurations: Configurations): World {
        val game = configurations.config.game
        return World(Vec2(0f, game.gravity)).apply {
            particleRadius = game.particleRadius
        }
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
        return OrthographicCamera().apply {
            setToOrtho(false, Gdx.graphics.width.meters, Gdx.graphics.height.meters)
        }
    }

    @Provides @Singleton @GuiCamera
    fun uiCamera() = OrthographicCamera().apply {
        setToOrtho(false)
    }

    @Provides @Singleton
    fun engine(): Engine = game.engine

    @Provides @Singleton
    fun messagingContactListener(messaging: Messaging) =
            MessagingContactListener(messaging)

    @Provides @Singleton
    fun tmxMapLoader() = TmxMapLoader(InternalFileHandleResolver())

    @Provides @Singleton @InternalMapLoader
    fun internalTmxMapLoader() = TmxMapLoader()

    @Provides @Singleton
    fun layeredTiledMapRenderer(camera: OrthographicCamera, batch: SpriteBatch) =
            JellyMapRenderer(camera, batch, Units.MPP)

    @Provides @Singleton
    fun scriptEngine() = ScriptEngineManager().getEngineByName("nashorn") as NashornScriptEngine

    @Provides @Singleton
    fun watchService(configurations: Configurations,
                     manager: ScriptManager): FileAlterationMonitor {

        return FileAlterationMonitor(2000).apply {
            if (configurations.config.game.debug) {
                addObserver(manager.createObserver())
            }
        }
    }

    @Provides @Singleton
    fun assetManager() = AssetManager()
}

data class Systems(val systems: List<Class<out EntitySystem>>)

@BindingAnnotation
annotation class GuiCamera

@BindingAnnotation
annotation class InternalMapLoader