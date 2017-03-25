package com.edd.jelly.core.configuration

class Config(
        val game: Game,
        val video: Video
) {

    /**
     * General game configuration settings.
     */
    class Game(val debug: Boolean)

    /**
     * Video configuration.
     */
    class Video(val screen: Screen, val fpsLimit: Int) {

        /**
         * Screen configuration.
         */
        class Screen(val fullscreen: Boolean, val width: Int, val height: Int)
    }
}