package com.edd.jelly.core

import com.badlogic.ashley.core.Engine
import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.OrthographicCamera
import com.edd.jelly.behaviour.level.LoadNewLevelEvent
import com.edd.jelly.behaviour.ui.LoadGameScreenEvent
import com.edd.jelly.core.configuration.ConfigurationChangeEventListener
import com.edd.jelly.core.configuration.Configurations
import com.edd.jelly.core.configuration.Configurations.Companion.MENU_LEVEL_NAME
import com.edd.jelly.core.events.Messaging
import com.edd.jelly.util.meters
import com.google.inject.Guice
import com.google.inject.Injector
import com.google.inject.Key
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

        messaging.listen(injector.getInstance(ConfigurationChangeEventListener::class.java))

        Gdx.input.inputProcessor = injector.getInstance(InputMultiplexer::class.java)

        configurations.config.game.debugLevel?.let {
            messaging.send(LoadNewLevelEvent(it))
            messaging.send(LoadGameScreenEvent)

        } ?: messaging.send(LoadNewLevelEvent(MENU_LEVEL_NAME, true))
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

    override fun resize(width: Int, height: Int) {
        super.resize(width, height)

        injector.getInstance(OrthographicCamera::class.java).apply {
            setToOrtho(false, width.meters, height.meters)
        }

        injector.getInstance(Key.get(OrthographicCamera::class.java, GuiCamera::class.java)).apply {
            setToOrtho(false, width.toFloat(), height.toFloat())
        }
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