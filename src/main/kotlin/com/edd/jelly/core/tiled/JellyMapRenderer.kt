package com.edd.jelly.core.tiled

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.maps.MapLayer
import com.badlogic.gdx.maps.tiled.TiledMapImageLayer
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.math.MathUtils

class JellyMapRenderer(private val camera: OrthographicCamera,
                       batch: Batch,
                       unitScale: Float) : OrthogonalTiledMapRenderer(null, unitScale, batch) {

    /**
     * Draw map background and background layers.
     */
    fun drawBackground(map: JellyMap) {

        // First render the background if it exists.
        map.background?.let {

            // Fullscreen.
            batch.projectionMatrix.setToOrtho2D(0f, 0f, camera.viewportWidth, camera.viewportHeight)
            beginRender()
            batch.draw(
                    it,
                    0f,
                    0f,
                    camera.viewportWidth,
                    camera.viewportHeight)
            endRender()

            // Reset projection matrix back to where it was.
            batch.projectionMatrix = camera.combined
        }

        // Render other background layers.
        beginRender()
        draw(map.backgroundLayers)
        endRender()
    }

    /**
     * Draw map foreground layers.
     */
    fun drawForeground(map: JellyMap) {
        beginRender()
        draw(map.foregroundLayers)
        endRender()
    }

    /**
     * General draw method for all layers.
     */
    private fun draw(layers: Iterable<MapLayer>) {
        layers.forEach {
            if (it.isVisible) {
                if (it is TiledMapTileLayer) {
                    renderTileLayer(it)
                } else if (it is TiledMapImageLayer) {
                    renderImageLayer(it)
                } else if (it is ParallaxLayer) {
                    drawParallaxLayer(it)
                } else {
                    renderObjects(it)
                }
            }
        }
    }

    /**
     * Handle parallax layer movement and drawing.
     */
    private fun drawParallaxLayer(layer: ParallaxLayer) {
        with(layer) {

            // Half viewport height.
            val hh = camera.viewportHeight / 2

            // Camera y position with offset.
            val offsetY = offset.y + camera.position.y

            // Camera bottom.
            val y = offsetY - hh

            // Parallax camera bottom.
            var py = y * speed.y

            // If bottom is clamped, make sure that parallax view
            // doesn't go above the camera viewport.
            if (clampBottom && py > y) {
                py = y
            }

            // If top is clamped, make sure that parallax view
            // doesn't go below the camera viewport.
            if (clampTop) {
                val topParallaxY = py + size.y
                val topY = y + camera.viewportHeight

                if (topY > topParallaxY) {
                    py += topY - topParallaxY
                }
            }

            // Camera left.
            val x = offset.x + camera.position.x - size.x / 2

            for (padding in -1..1) {
                batch.draw(
                        texture,
                        x * speed.x + MathUtils.round(x * (1 - speed.x) / size.x) * size.x + padding * size.x,
                        py,
                        size.x,
                        size.y
                )
            }
        }
    }
}