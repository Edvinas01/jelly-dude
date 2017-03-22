package com.edd.jelly.util

import com.edd.jelly.behaviour.physics.DebugRenderer
import org.apache.logging.log4j.LogManager

class UncaughtExceptionLogger : Thread.UncaughtExceptionHandler {

    companion object {
        private val LOG = LogManager.getLogger(DebugRenderer::class.java)
    }

    override fun uncaughtException(t: Thread, e: Throwable) {
        LOG.fatal("Uncaught exception for thread: {}", t, e)
    }
}