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

// Awesome https://github.com/pabloogc/LiveWater
@Singleton
class LiquidRenderer @Inject constructor(
        private val world: World
) {

    private companion object {
        const val VERTEX_PER_DROPLET = 3
        const val INDEX_PER_DROPLET = 3
        const val TRIANGLE_SCALE = 2.5f
        const val POTENTIAL_MAP_SIZE = 128

        val INDEX_OFFSET = shortArrayOf(0, 1, 2)

        val ATTRIBUTES = VertexAttributes(
                VertexAttribute(VertexAttributes.Usage.Position, 3, "center"),
                VertexAttribute(VertexAttributes.Usage.Position, 3, "position")
        )
    }

    init {
        println(ATTRIBUTES.vertexSize)
        println(ATTRIBUTES.vertexSize * world.particleMaxCount)
    }

    // Is 6 - component count, vertices(3) + indices(3).
    private val vertexBuffer = FloatArray(VERTEX_PER_DROPLET * 6 * world.particleMaxCount)

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
        val buffer = world.particlePositionBuffer
        var idx = 0

        for (i in 0..world.particleCount - 1) {
            val pos = buffer[i]

            val scl = TRIANGLE_SCALE

            // Top center.
            idx = writeVertex(idx, pos.x + 0.0f * scl, pos.y + 0.622008459f * scl, pos)

            // Bottom left.
            idx = writeVertex(idx, pos.x - 0.5f * scl, pos.y - 0.311004243f * scl, pos)

            // Bottom right.
            idx = writeVertex(idx, pos.x + 0.5f * scl, pos.y - 0.311004243f * scl, pos)
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
    private fun writeVertex(pointer: Int, x: Float, y: Float, pos: Vec2): Int {
        var idx = pointer

        // Center.
        vertexBuffer[idx++] = pos.x
        vertexBuffer[idx++] = pos.y
        vertexBuffer[idx++] = 0f

        // Position.
        vertexBuffer[idx++] = x
        vertexBuffer[idx++] = y
        vertexBuffer[idx++] = 0f

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
}