package com.edd.jelly.core.configuration

data class Config(
        val game: Game,
        val video: Video,
        val input: Input
) {

    /**
     * General game configuration settings.
     */
    data class Game(var language: String,
                    var scripting: Boolean,
                    val debugLevel: String?,
                    val gravity: Float,
                    val particleRadius: Float,
                    val maxParticles: Int,
                    var debug: Boolean
    )

    /**
     * Video configuration.
     */
    data class Video(val screen: Screen, val fpsLimit: Int, val vsync: Boolean) {

        /**
         * Screen configuration.
         */
        data class Screen(var fullscreen: Boolean, val width: Int, val height: Int)
    }

    /**
     * Input settings.
     */
    data class Input(
            var up: String,
            var down: String,
            var left: String,
            var right: String,
            var shrink: String,
            var stick: String,
            var reset: String
    )
}