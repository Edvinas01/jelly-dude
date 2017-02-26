package com.edd.jelly.behaviour.rendering

import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.maps.MapLayer
import com.badlogic.gdx.maps.tiled.TiledMapImageLayer
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer

class LayeredTiledMapRenderer(batch: Batch,
                              unitScale: Float) : OrthogonalTiledMapRenderer(null, unitScale, batch) {


    fun render(layers: List<MapLayer>) {
        beginRender()
        layers.forEach {
            if (it.isVisible) {
                if (it is TiledMapTileLayer) {
                    renderTileLayer(it)
                } else if (it is TiledMapImageLayer) {
                    renderImageLayer(it)
                } else {
                    renderObjects(it)
                }
            }
        }
        endRender()
    }
}