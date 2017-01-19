package com.edd.jelly

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.backends.lwjgl.LwjglApplication
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration

class Launcher : ApplicationAdapter() {

    override fun render() {
        println("running, dt: " + Gdx.graphics.deltaTime)
    }

    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            val configuration = LwjglApplicationConfiguration()

            configuration.foregroundFPS = 60
            configuration.width = 800
            configuration.height = 600

            configuration.fullscreen = false
            configuration.resizable = false

            LwjglApplication(LiquidFunTest(), configuration)
        }
    }
}