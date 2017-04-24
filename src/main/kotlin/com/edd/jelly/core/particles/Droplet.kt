package com.edd.jelly.core.particles

import com.badlogic.gdx.Application
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Gdx.*
import com.badlogic.gdx.Graphics
import com.badlogic.gdx.graphics.*
import com.badlogic.gdx.graphics.VertexAttributes.Usage
import com.badlogic.gdx.graphics.glutils.FrameBuffer
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.NumberUtils
import com.edd.jelly.util.toVector2
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.World


class Droplet {

    companion object {
        const val MAX_PARTICLES = 10000
        const val TRIANGLE_SCALE = 3.3f
        const val VERTEX_PER_DROPLET = 3
        const val INDEX_PER_DROPLET = 3
        const val POTENTIAL_MAP_SIZE = 256

        lateinit var world: World
        lateinit var camera: OrthographicCamera
        lateinit var potentialFbo: FrameBuffer

        val INDEX_OFFSET = shortArrayOf(0, 1, 2)
        val ATTRIBUTES = VertexAttributes(
                VertexAttribute(Usage.Position, 3, "center"),
                VertexAttribute(Usage.Position, 3, "position")
        )

        //RENDER
        val DROPLET_BUFFER = FloatArray(ATTRIBUTES.vertexSize * MAX_PARTICLES / 4)
        val INDEX_BUFFER = ShortArray(VERTEX_PER_DROPLET * MAX_PARTICLES,
                { i ->
                    val offset = INDEX_OFFSET[i % INDEX_PER_DROPLET]
                    ((i / INDEX_PER_DROPLET) * VERTEX_PER_DROPLET + offset).toShort()
                }
        )

        val particleMesh = Mesh(
                /*isStatic*/    false,
                /*maxVertices*/ MAX_PARTICLES * VERTEX_PER_DROPLET,
                /*maxIndices*/  MAX_PARTICLES * INDEX_PER_DROPLET,
                /*attributes*/  ATTRIBUTES)
                .apply {
                    setAutoBind(false)
                    setVertices(DROPLET_BUFFER)
                    setIndices(INDEX_BUFFER)
                }

        val potentialShader = ShaderProgram(POTENTIAL_VERTEX_SHADER, POTENTIAL_FRAGMENT_SHADER).assertCompiled()
        val potentailAttributes = potentialShader.findLocations(ATTRIBUTES)

        val normalShader = ShaderProgram(LIQUID_VERTEX_SHADER, LIQUID_FRAGMENT_SHADER).assertCompiled()
        val normalAttributes = normalShader.findLocations(ATTRIBUTES)

        fun create() {
            potentialFbo = FrameBuffer(Pixmap.Format.RGBA8888, POTENTIAL_MAP_SIZE, POTENTIAL_MAP_SIZE, false)
            potentialFbo.colorBufferTexture.setWrap(Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.ClampToEdge)
            potentialFbo.colorBufferTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        }


        fun render() {
            val count = pack()

            potentialFbo.begin() //Draw this to a texture
            drawPotential(count)
            potentialFbo.end()

            //Normal draw
            drawNormal(count)
        }


        private fun drawPotential(count: Int) {
            gl.glViewport(0, 0, POTENTIAL_MAP_SIZE, POTENTIAL_MAP_SIZE)

            gl.glEnable(GL20.GL_BLEND)
            gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
            gl.glClearColor(0f, 0f, 0f, 1f)
            gl.glClear(GL20.GL_COLOR_BUFFER_BIT or GL20.GL_DEPTH_BUFFER_BIT)

            potentialShader.begin()
            potentialShader.setUniformMatrix("mvp", camera.combined)

            particleMesh.bind(potentialShader, potentailAttributes)
            particleMesh.render(potentialShader, GL20.GL_TRIANGLES, 0, count)
            particleMesh.unbind(potentialShader)

            potentialShader.end()

            gl.glDisable(GL20.GL_BLEND)
        }

        private fun drawNormal(count: Int) {
//            gl.glViewport(0, 0, Gdx.app.graphics.width, Gdx.app.graphics.height)

            gl.glEnable(GL20.GL_BLEND)
            gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)

            normalShader.begin()
            potentialFbo.colorBufferTexture.bind(0)
            normalShader.setUniformi("tex", 0)
            normalShader.setUniformMatrix("mvp", camera.combined)

            particleMesh.bind(normalShader, normalAttributes)
            particleMesh.render(normalShader, GL20.GL_TRIANGLES, 0, count)
            particleMesh.unbind(normalShader)

            normalShader.end()

            gl.glDisable(GL20.GL_BLEND)
        }

        private fun pack(): Int {
            var idx = 0

            val writeVertex = fun(x: Float, y: Float, pos: Vec2) {
                //center
                DROPLET_BUFFER[idx++] = pos.x
                DROPLET_BUFFER[idx++] = pos.y
                DROPLET_BUFFER[idx++] = 0f

                //position
                DROPLET_BUFFER[idx++] = x
                DROPLET_BUFFER[idx++] = y
                DROPLET_BUFFER[idx++] = 0f
            }

            for (i in 0..world.particleCount - 1) {
                val pos = world.particlePositionBuffer[i]
                writeVertex(pos.x + 0.0f * TRIANGLE_SCALE, pos.y + 0.622008459f * TRIANGLE_SCALE, pos) // top
                writeVertex(pos.x - 0.5f * TRIANGLE_SCALE, pos.y - 0.311004243f * TRIANGLE_SCALE, pos) // bottom left
                writeVertex(pos.x + 0.5f * TRIANGLE_SCALE, pos.y - 0.311004243f * TRIANGLE_SCALE, pos) // bottom right
            }

            particleMesh.updateVertices(0, DROPLET_BUFFER, 0, idx)

            return world.particleCount * INDEX_PER_DROPLET
        }
    }
}

//language=GLSL
private const val POTENTIAL_VERTEX_SHADER = """
    uniform mat4 mvp;

    attribute vec3 center;
    attribute vec3 position;

    varying vec3 c;
    varying vec3 p;

    void main(){
        c = center;
        p = position;
        gl_Position = mvp * vec4(position.xyz, 1.0);
    }
"""

//language=GLSL
private const val POTENTIAL_FRAGMENT_SHADER = """
    varying vec3 c;
    varying vec3 p;

    void main(){
        float dx = c.x - p.x;
        float dy = c.y - p.y;
        float d = (dx * dx + dy * dy); //Distance squared. Also potential is: CONSTANT / (d * d), but we can skip that
        gl_FragColor = vec4(0.0, 0.0, 1.0, max(1.0 - d, 0.0));
    }
"""

//language=GLSL
private const val LIQUID_VERTEX_SHADER = """
    uniform mat4 mvp;

    attribute vec3 position;
    varying vec2 t;

    void main(){
        vec4 clipPosition = mvp * vec4(position.xyz, 1.0);
        //Map the vertex to the buffer texture that fills the screen
        //No need to divide by w, we are using orthogonal projection so its always 1
        t = (clipPosition.xy + vec2(1.0, 1.0)) / 2.0;
        gl_Position = clipPosition;
    }
"""

//language=GLSL
private const val LIQUID_FRAGMENT_SHADER = """
    uniform sampler2D tex;
    varying vec2 t;

    const float step = 0.005;
    const int samples = 5;
    const vec2 lightPosition = normalize(vec2(0.0, 1.0));
    const float depthColorShift = 0.8;
    const vec4 mixColor = vec4(depthColorShift, depthColorShift, depthColorShift, 1.0);

    void main(){
        vec4 c = texture2D(tex, t);
        if(c.b > 0.7){
            float depth = 0;
            vec4 outColor = vec4(0.01, 0.13, c.b, c.b);
            for(int i = 0; i < samples; i++){
                depth += texture2D(tex, t + (step * i * lightPosition)).b / samples;
            }
            outColor = mix(mixColor, outColor, depth);
            gl_FragColor = outColor;
        }
        else discard;
    }
"""

//Shaders
fun ShaderProgram.assertCompiled() = apply { if (!isCompiled) error("Shader compilation failed:\n" + log) }

fun ShaderProgram.findLocation(attr: VertexAttribute) = getAttributeLocation(attr.alias)
fun ShaderProgram.findLocations(attrs: VertexAttributes) = attrs.map { findLocation(it) }.toIntArray()