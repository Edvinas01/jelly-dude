package com.edd.jelly.core.particles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.edd.jelly.exception.GameException;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.World;
import org.jbox2d.particle.ParticleColor;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static com.badlogic.gdx.graphics.VertexAttributes.Usage.ColorUnpacked;
import static com.badlogic.gdx.graphics.VertexAttributes.Usage.Position;
import static com.badlogic.gdx.graphics.glutils.ShaderProgram.COLOR_ATTRIBUTE;
import static com.badlogic.gdx.graphics.glutils.ShaderProgram.POSITION_ATTRIBUTE;
import static com.badlogic.gdx.graphics.glutils.ShaderProgram.prependVertexCode;
import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL15.GL_DYNAMIC_DRAW;
import static org.lwjgl.opengl.GL31.GL_TEXTURE_BUFFER;

@Singleton
public class Blober {

    private final ShaderProgram program;
    private final World world;
    private final Mesh mesh;

    @Inject
    public Blober(World world) {
        this.world = world;
        this.program = new ShaderProgram(
                Gdx.files.internal("glsl/blob.vert"),
                Gdx.files.internal("glsl/blob.frag")
        );

        if (!program.isCompiled()) {
            throw new GameException(program.getLog());
        }

        this.mesh = new Mesh(
                false,
                world.getParticleMaxCount(),
                world.getParticleMaxCount(),
                new VertexAttribute(Position, 3, POSITION_ATTRIBUTE),
                new VertexAttribute(ColorUnpacked, 3, COLOR_ATTRIBUTE)
        );
    }

    public void render(Matrix4 projectionMatrix) {
        Gdx.gl20.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        float[] data = new float[world.getParticleCount() * 3];
        int idx = 0;

        for (int i = 0; i < world.getParticleCount(); i++) {
            Vec2 pos = world.getParticlePositionBuffer()[i];
            data[idx++] = pos.x;
            data[idx++] = pos.y;
            data[idx++] = 0;
        }


        program.begin();

        program.setUniformMatrix("u_projTrans", projectionMatrix);
//        program.setUniformi("blobCount", world.getParticleCount());
//        program.setUniform3fv("positions", data, 0, data.length);

        mesh.setVertices(data);
        mesh.render(program, GL_TRIANGLES, 0, world.getParticleCount());

        program.end();

        if (program.getLog() != null && !program.getLog().isEmpty()) {
            System.out.println(program.getLog());
        }
    }
}
