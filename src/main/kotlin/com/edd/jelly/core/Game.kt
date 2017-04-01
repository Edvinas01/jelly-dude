package com.edd.jelly.core

import com.badlogic.ashley.core.Engine
import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.edd.jelly.core.configuration.Configurations
import com.edd.jelly.core.events.Messaging
import com.google.inject.Guice
import com.google.inject.Injector
import org.apache.commons.io.monitor.FileAlterationMonitor
import org.apache.logging.log4j.LogManager
import kotlin.system.measureTimeMillis

class Game(val configurations: Configurations) : ApplicationAdapter() {

    companion object {
        private val LOG = LogManager.getLogger(Game::class.java)
    }

    internal lateinit var engine: Engine
    internal lateinit var injector: Injector

    val messaging = Messaging().stop()

    init {
        configurations.setup(this)
    }

    override fun create() {
        LOG.info("Starting game")

        engine = Engine()
        injector = Guice.createInjector(GameModule(this))

        initSystems()

        injector.getInstance(Messaging::class.java)
                .start()

        injector.getInstance(FileAlterationMonitor::class.java)
                .start()

        Gdx.input.inputProcessor = injector.getInstance(InputMultiplexer::class.java)
    }

    override fun render() {
        engine.update(Gdx.graphics.deltaTime)
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