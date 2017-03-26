package com.edd.jelly

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.backends.lwjgl.LwjglApplication
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration
import com.edd.jelly.core.Game
import com.edd.jelly.core.configuration.Configurations
import com.edd.jelly.util.UncaughtExceptionLogger

class Launcher : ApplicationAdapter() {

    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            Thread.setDefaultUncaughtExceptionHandler(UncaughtExceptionLogger())

            val configurations = Configurations()
            LwjglApplication(Game(configurations), LwjglApplicationConfiguration().apply {
                val video = configurations.config.video
                foregroundFPS = video.fpsLimit

                val screen = video.screen
                width = screen.width
                height = screen.height
                fullscreen = screen.fullscreen

                // Cannot resize for now.
                resizable = false
            })
        }
    }
}