package com.edd.jelly.core.particles;

import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.edd.jelly.exception.GameException;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jbox2d.dynamics.World;

import static com.badlogic.gdx.graphics.VertexAttributes.Usage.ColorUnpacked;
import static com.badlogic.gdx.graphics.VertexAttributes.Usage.Position;
import static com.badlogic.gdx.graphics.glutils.ShaderProgram.COLOR_ATTRIBUTE;
import static com.badlogic.gdx.graphics.glutils.ShaderProgram.POSITION_ATTRIBUTE;

@Singleton
public class ParticleRenderer {

    private static final Logger LOG = LogManager.getLogger(ParticleRenderer.class);

    private final ShaderProgram shader;
    private final Texture diffuse;
    private final World world;
    private final Mesh mesh;

    private final Blober blober;

    private float[] positions;

    @Inject
    public ParticleRenderer(World world, Blober blober) {
        this.blober = blober;
        this.shader = createShader();
        this.diffuse = createTexture();
        this.world = world;
        this.mesh = createMesh(world);

        Droplet.Companion.create();
    }

    private Mesh createMesh(World world) {
        return new Mesh(
                false,
                world.getParticleMaxCount(),
                world.getParticleMaxCount(),
                new VertexAttribute(Position, 2, POSITION_ATTRIBUTE),
                new VertexAttribute(ColorUnpacked, 4, COLOR_ATTRIBUTE)
        );
    }

    public void render(OrthographicCamera camera) {
        blober.render(camera.combined);


        Droplet.camera = camera;
        Droplet.world = world;

//        Droplet.Companion.render();

//        // Re-initialize particle position array if needed.
//        Vec2[] particles = world.getParticlePositionBuffer();
//        if (positions == null || positions.length != particles.length * 2) {
//            positions = new float[particles.length * 2];
//        }
//
//        // Update particle positions.
//        for (int i = 0; i < world.getParticleCount(); i++) {
//            Vec2 particle = particles[i];
//            positions[i * 2] = particle.x;
//            positions[i * 2 + 1] = particle.y;
//        }
//
//        Gdx.gl20.glEnable(GL20.GL_BLEND);
//        Gdx.gl20.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
//        Gdx.gl20.glEnable(GL20.GL_VERTEX_PROGRAM_POINT_SIZE);
//        Gdx.gl20.glEnable(0x8861);
//
//        shader.begin();
//
//        shader.setUniformf("particlesize", world.getParticleRadius() * 5);
//        shader.setUniformf("scale", 1f);
//        shader.setUniformMatrix("u_projTrans", projectionMatrix);
//
//        mesh.setVertices(positions);
//        mesh.render(shader, GL20.GL_POINTS, 0, world.getParticleCount());
//
//        shader.end();
    }

    private Texture createTexture() {
        return null;
    }

    private ShaderProgram createShader() {
        ShaderProgram program = new ShaderProgram(
                vertexShader,
                fragmentShader
        );

        if (!program.isCompiled()) {
            throw new GameException("Could not compile particle shader: " + program.getLog());
        }
        return program;
    }

    final String vertexShader = ""
            + "attribute vec4 a_position;\n"
            + "\n"
            + "uniform float particlesize;\n"
            + "uniform float scale;\n"
            + "uniform mat4 u_projTrans;\n"
            + "\n"
            + "attribute vec4 a_color;\n"
            + "varying vec4 v_color;\n"
            + "\n"
            + "void main()\n"
            + "{\n"
            + "   gl_Position =  u_projTrans * vec4(a_position.xy, 0.0, 1.0);\n"
            + "   gl_PointSize = scale * particlesize;\n"
            + "   v_color = a_color;"
            + "}\n";

    final String fragmentShader = ""
            + "#ifdef GL_ES\n"
            + "#define LOWP lowp\n"
            + "precision mediump float;\n"
            + "#else\n"
            + "#define LOWP \n"
            + "#endif\n"
            + "varying vec4 v_color;\n"
            + "void main()\n"
            + "{\n"
            + " float len = length(vec2(gl_PointCoord.x - 0.5, gl_PointCoord.y - 0.5));\n"
            + " if(len <= 0.5) {\n"
            + " 	gl_FragColor = v_color;\n"
            + " } else {\n"
            + " 	gl_FragColor = v_color;\n"
            + " }\n"
            + "}";
}