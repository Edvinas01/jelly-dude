package com.edd.jelly.util

import com.edd.jelly.behaviour.physics.PhysicsDebugRenderer
import com.edd.jelly.core.configuration.Configurations
import org.apache.logging.log4j.LogManager
import javax.swing.SwingUtilities

class UncaughtExceptionLogger : Thread.UncaughtExceptionHandler {

    companion object {
        private val LOG = LogManager.getLogger(PhysicsDebugRenderer::class.java)
    }

    override fun uncaughtException(t: Thread, e: Throwable) {

        LOG.fatal("Uncaught exception for thread: {}", t, e)
        SwingUtilities.invokeLater {
            ErrorReport(Configurations.GIT_HUB_NEW_ISSUE_URL, e).apply {
                pack()
                isVisible = true
            }
        }
    }
}