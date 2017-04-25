package com.edd.jelly.behaviour.physics

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.*
import com.badlogic.gdx.graphics.glutils.FrameBuffer
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.math.Matrix4
import com.edd.jelly.exception.GameException
import com.google.inject.Inject
import com.google.inject.Singleton
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.World
import org.jbox2d.particle.ParticleColor

// Awesome https://github.com/pabloogc/LiveWater
@Singleton
class LiquidRenderer @Inject constructor(
        private val world: World
) {

    private companion object {
        const val VERTEX_PER_DROPLET = 3
        const val INDEX_PER_DROPLET = 3
        const val TRIANGLE_SCALE = 1.5f
        const val POTENTIAL_MAP_SIZE = 128

        val INDEX_OFFSET = shortArrayOf(0, 1, 2)

        val ATTRIBUTES = VertexAttributes(
                VertexAttribute(VertexAttributes.Usage.Position, 2, "center"),
                VertexAttribute(VertexAttributes.Usage.Position, 2, "position"),
                VertexAttribute(VertexAttributes.Usage.ColorUnpacked, 4, "color")
        )
    }

    // Is 4 - component count = center(2) + position(2) + color(8).
    private val vertexBuffer = FloatArray(VERTEX_PER_DROPLET * 8 * world.particleMaxCount)

    private val indexBuffer = ShortArray(VERTEX_PER_DROPLET * world.particleMaxCount, { i ->
        ((i / INDEX_PER_DROPLET) * VERTEX_PER_DROPLET + INDEX_OFFSET[i % INDEX_PER_DROPLET]).toShort()
    })

    private val particleMesh = Mesh(
            false,
            world.particleMaxCount * VERTEX_PER_DROPLET,
            world.particleMaxCount * INDEX_PER_DROPLET,
            ATTRIBUTES
    ).apply {
        setAutoBind(false)
        setVertices(vertexBuffer)
        setIndices(indexBuffer)
    }

    private val potentialShader = loadShader("potential")
    private val liquidShader = loadShader("liquid")

    private val potentialAttributes = potentialShader.attributeArray()
    private val liquidAttributes = liquidShader.attributeArray()

    private val frameBuffer = FrameBuffer(
            Pixmap.Format.RGBA8888,
            POTENTIAL_MAP_SIZE,
            POTENTIAL_MAP_SIZE,
            false
    ).apply {
        colorBufferTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        colorBufferTexture.setWrap(Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.ClampToEdge)
    }

    /**
     * Render liquid fun particles.
     */
    fun render(camera: Camera) {
        val projection = camera.combined

        val count = updateVertices()
        renderPotential(count, projection)
        renderLiquid(count, projection)
    }

    /**
     * Update mesh vertices.
     */
    private fun updateVertices(): Int {
        val positionBuffer = world.particlePositionBuffer
        val colorBuffer = world.particleColorBuffer

        var idx = 0

        for (i in 0..world.particleCount - 1) {
            val pos = positionBuffer[i]
            val color = colorBuffer[i]

            // In order: top center, bottom left, bottom right.
            idx = writeVertex(idx, pos.x + 0.0f * TRIANGLE_SCALE, pos.y + 0.622008459f * TRIANGLE_SCALE, pos, color)
            idx = writeVertex(idx, pos.x - 0.5f * TRIANGLE_SCALE, pos.y - 0.311004243f * TRIANGLE_SCALE, pos, color)
            idx = writeVertex(idx, pos.x + 0.5f * TRIANGLE_SCALE, pos.y - 0.311004243f * TRIANGLE_SCALE, pos, color)
        }
        particleMesh.updateVertices(0, vertexBuffer, 0, idx)

        return world.particleCount * INDEX_PER_DROPLET
    }

    /**
     * Render potential shader and write to frame buffer.
     */
    private fun renderPotential(count: Int, projection: Matrix4) {
        frameBuffer.begin()

        Gdx.gl.glViewport(0, 0, POTENTIAL_MAP_SIZE, POTENTIAL_MAP_SIZE)

        Gdx.gl.glEnable(GL20.GL_BLEND)
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT or GL20.GL_DEPTH_BUFFER_BIT)

        potentialShader.begin()
        potentialShader.setUniformMatrix("mvp", projection)
        potentialShader.setUniformMatrix("mvp", projection)
        potentialShader.setUniformf("radius", world.particleRadius)

        particleMesh.bind(potentialShader, potentialAttributes)
        particleMesh.render(potentialShader, GL20.GL_TRIANGLES, 0, count)
        particleMesh.unbind(potentialShader)

        potentialShader.end()

        Gdx.gl.glDisable(GL20.GL_BLEND)

        frameBuffer.end()
    }

    /**
     * Render liquid shader.
     */
    private fun renderLiquid(count: Int, projection: Matrix4) {
        Gdx.gl.glViewport(0, 0, Gdx.graphics.width, Gdx.graphics.height)

        Gdx.gl.glEnable(GL20.GL_BLEND)
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)

        liquidShader.begin()
        frameBuffer.colorBufferTexture.bind(0)
        liquidShader.setUniformi("tex", 0)
        liquidShader.setUniformMatrix("mvp", projection)

        particleMesh.bind(liquidShader, liquidAttributes)
        particleMesh.render(liquidShader, GL20.GL_TRIANGLES, 0, count)
        particleMesh.unbind(liquidShader)

        liquidShader.end()

        Gdx.gl.glDisable(GL20.GL_BLEND)
    }

    /**
     * Write vertex to vertex buffer.
     *
     * @return pointer to the next vertex.
     */
    private fun writeVertex(pointer: Int, x: Float, y: Float, position: Vec2, color: ParticleColor): Int {
        var idx = pointer

        // Center.
        vertexBuffer[idx++] = position.x
        vertexBuffer[idx++] = position.y

        // Position.
        vertexBuffer[idx++] = x
        vertexBuffer[idx++] = y

        // Color.
        vertexBuffer[idx++] = color.r.toFloatColor()
        vertexBuffer[idx++] = color.g.toFloatColor()
        vertexBuffer[idx++] = color.b.toFloatColor()
        vertexBuffer[idx++] = color.a.toFloatColor()

        return idx
    }

    /**
     * Load shader by name and check for compile errors.
     */
    private fun loadShader(name: String): ShaderProgram {
        val program = ShaderProgram(
                Gdx.files.internal("shaders/$name.vert"),
                Gdx.files.internal("shaders/$name.frag")
        )

        if (!program.isCompiled) {
            throw GameException("Error in $name shader: ${program.log}")
        }
        return program
    }

    /**
     * Helper function to fetch attribute array from the shader.
     */
    private fun ShaderProgram.attributeArray(): IntArray {
        return ATTRIBUTES.map {
            this.getAttributeLocation(it.alias)
        }.toIntArray()
    }

    /**
     * Convert signed byte value to float color value.
     */
    private fun Byte.toFloatColor(): Float {
        return (this.toInt() and 0xff) / 255f
    }
}