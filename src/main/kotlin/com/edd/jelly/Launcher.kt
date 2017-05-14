package com.edd.jelly

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.backends.lwjgl.LwjglApplication
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration
import com.edd.jelly.game.JellyGame
import com.edd.jelly.core.configuration.Configurations
import com.edd.jelly.util.UncaughtGameExceptionHandler
import com.xenomachina.argparser.ArgParser

class Launcher : ApplicationAdapter() {

    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            Thread.setDefaultUncaughtExceptionHandler(UncaughtGameExceptionHandler())

            val configurations = Configurations(ArgParser(args))
            LwjglApplication(JellyGame(configurations), LwjglApplicationConfiguration().apply {
                val video = configurations.config.video
                foregroundFPS = video.fpsLimit

                val screen = video.screen
                width = screen.width
                height = screen.height
                fullscreen = screen.fullscreen
                vSyncEnabled = video.vsync

                // Cannot resize for now.
                resizable = false
            })
        }
    }
}