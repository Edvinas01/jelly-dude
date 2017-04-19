package com.edd.jelly.core.configuration

data class Config(
        val game: Game,
        val video: Video
) {

    /**
     * General game configuration settings.
     */
    data class Game(var language: String,
                    val scripting: Boolean,
                    val debugLevel: String?,
                    val debug: Boolean,
                    val gravity: Float,
                    val particleRadius: Float)

    /**
     * Video configuration.
     */
    data class Video(val screen: Screen, val fpsLimit: Int) {

        /**
         * Screen configuration.
         */
        data class Screen(var fullscreen: Boolean, val width: Int, val height: Int)
    }
}