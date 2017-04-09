package com.edd.jelly.core

import com.badlogic.ashley.core.Engine
import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.assets.AssetManager
import com.edd.jelly.behaviour.level.LoadNewLevelEvent
import com.edd.jelly.core.configuration.Configurations
import com.edd.jelly.core.events.Messaging
import com.google.inject.Guice
import com.google.inject.Injector
import org.apache.commons.io.monitor.FileAlterationMonitor
import org.apache.logging.log4j.LogManager
import kotlin.system.measureTimeMillis

class JellyGame(val configurations: Configurations) : Game() {

    companion object {
        private val LOG = LogManager.getLogger(JellyGame::class.java)
    }

    internal val engine = Engine()

    internal lateinit var assetManager: AssetManager
    internal lateinit var injector: Injector

    val messaging = Messaging().stop()

    init {
        configurations.setup(this)
    }

    override fun create() {
        LOG.info("Starting game")

        injector = Guice.createInjector(GameModule(this))

        assetManager = injector.getInstance(AssetManager::class.java)

        initSystems()

        injector.getInstance(Messaging::class.java)
                .start()

        injector.getInstance(FileAlterationMonitor::class.java)
                .start()

        Gdx.input.inputProcessor = injector.getInstance(InputMultiplexer::class.java)

//        messaging.send(LoadNewLevelEvent(MENU_LEVEL_NAME, true)) // todo BLOOP :V
        messaging.send(LoadNewLevelEvent("test", false))
    }

    override fun render() {

        // Make sure all the assets are loaded before doing anything.
        if (!assetManager.update()) {
            return
        }
        engine.update(Gdx.graphics.deltaTime)

        super.render()
    }

    override fun dispose() {
        super.dispose()
    }

    /**
     * Initialize systems.
     */
    private fun initSystems() {
        val millis = measureTimeMillis {
            injector.getInstance(Systems::class.java).systems.map {
                injector.getInstance(it)
            }.forEach { s ->
                engine.addSystem(s)
            }
        }
        LOG.info("Started systems after {}ms", millis)
    }
}