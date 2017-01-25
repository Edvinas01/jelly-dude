package com.edd.jelly

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.backends.lwjgl.LwjglApplication
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration
import com.edd.jelly.core.Game
import com.edd.jelly.util.Configuration

class Launcher : ApplicationAdapter() {
// todo https://www.youtube.com/watch?v=3lC9UaKF6MA&index=8&list=PLbH4r1N8PmBLHriCYXFNBdbwa33yTd4YY
    companion object {

        @JvmStatic
        fun main(args: Array<String>) {

            val configuration = LwjglApplicationConfiguration()

            configuration.foregroundFPS = Configuration.FPS_LIMIT
            configuration.width = Configuration.SCREEN_WIDTH
            configuration.height = Configuration.SCREEN_HEIGHT

            configuration.fullscreen = Configuration.FULLSCREEN
            configuration.resizable = Configuration.RESIZABLE

            LwjglApplication(Game(), configuration)
        }
    }
}