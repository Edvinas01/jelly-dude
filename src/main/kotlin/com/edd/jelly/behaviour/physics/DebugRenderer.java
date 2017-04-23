package com.edd.jelly.behaviour.physics;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Disposable;
import org.jbox2d.collision.WorldManifold;
import org.jbox2d.collision.shapes.ChainShape;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.EdgeShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Transform;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.contacts.Contact;
import org.jbox2d.dynamics.joints.Joint;
import org.jbox2d.dynamics.joints.JointType;
import org.jbox2d.dynamics.joints.PulleyJoint;
import org.jbox2d.particle.ParticleColor;

// Thanks https://github.com/AchrafAmil/Box2DDebugRenderer !!!
public class DebugRenderer implements Disposable {

    /**
     * the immediate mode renderer to output our debug drawings
     **/
    private ShapeRenderer renderer;

    /**
     * vertices for polygon rendering
     **/
    private final static Vec2[] vertices = new Vec2[1000];

    private final static Vec2 lower = new Vec2();
    private final static Vec2 upper = new Vec2();

    private boolean drawBodies;
    private boolean drawJoints;
    private boolean drawAABBs;
    private boolean drawInactiveBodies;
    private boolean drawVelocities;
    private boolean drawContacts;

    public DebugRenderer(boolean drawBodies, boolean drawJoints, boolean drawAABBs, boolean drawInactiveBodies,
                         boolean drawVelocities, boolean drawContacts) {
        // next we setup the immediate mode renderer
        renderer = new ShapeRenderer();

        // initialize vertices array
        for (int i = 0; i < vertices.length; i++)
            vertices[i] = new Vec2();

        this.drawBodies = drawBodies;
        this.drawJoints = drawJoints;
        this.drawAABBs = drawAABBs;
        this.drawInactiveBodies = drawInactiveBodies;
        this.drawVelocities = drawVelocities;
        this.drawContacts = drawContacts;
    }

    /**
     * This assumes that the projection matrix has already been set.
     */
    public void render(World world, Matrix4 projectionMatrix) {
        renderer.setProjectionMatrix(projectionMatrix);
        renderBodies(world);
        renderParticles(world);
    }

    private final Color SHAPE_NOT_ACTIVE = new Color(0.5f, 0.5f, 0.3f, 1);
    private final Color SHAPE_STATIC = new Color(0.5f, 0.9f, 0.5f, 1);
    private final Color SHAPE_KINEMATIC = new Color(0.5f, 0.5f, 0.9f, 1);
    private final Color SHAPE_NOT_AWAKE = new Color(0.6f, 0.6f, 0.6f, 1);
    private final Color SHAPE_AWAKE = new Color(0.9f, 0.7f, 0.7f, 1);
    private final Color JOINT_COLOR = new Color(0.5f, 0.8f, 0.8f, 1);
    private final Color AABB_COLOR = new Color(1.0f, 0, 1.0f, 1f);
    private final Color VELOCITY_COLOR = new Color(1.0f, 0, 0f, 1f);
    private final Color PARTICLE_COLOR = new Color(0, 0.75f, 1f, 1f);

    private void renderBodies(World world) {
        renderer.begin(ShapeType.Line);

        if (drawBodies || drawAABBs) {
            for (Body body = world.getBodyList(); body != null; body = body.getNext()) {
                if (body.isActive() || drawInactiveBodies) renderBody(body);
            }
        }

        if (drawJoints) {
            for (Joint joint = world.getJointList(); joint != null; joint = joint.getNext()) {
                drawJoint(joint);
            }
        }
        renderer.end();
        if (drawContacts) {
            renderer.begin(ShapeType.Point);
            for (Contact contact = world.getContactList(); contact != null; contact = contact.getNext()) {
                drawContact(contact);
            }
            renderer.end();
        }
    }

    private void renderBody(Body body) {
        Transform transform = body.getTransform();
        for (Fixture fixture = body.getFixtureList(); fixture != null; fixture = fixture.getNext()) {
            if (drawBodies) {
                drawShape(fixture, transform, getColorByBody(body));
                if (drawVelocities) {
                    Vec2 position = body.getPosition();
                    drawSegment(position, body.getLinearVelocity().add(position), VELOCITY_COLOR);
                }
            }

            if (drawAABBs) {
                drawAABB(fixture, transform);
            }
        }
    }

    private Color getColorByBody(Body body) {
        if (!body.isActive())
            return SHAPE_NOT_ACTIVE;
        else if (body.getType() == BodyType.STATIC)
            return SHAPE_STATIC;
        else if (body.getType() == BodyType.KINEMATIC)
            return SHAPE_KINEMATIC;
        else if (!body.isAwake())
            return SHAPE_NOT_AWAKE;
        else
            return SHAPE_AWAKE;
    }

    private void drawAABB(Fixture fixture, Transform transform) {
        if (fixture.getType() == org.jbox2d.collision.shapes.ShapeType.CIRCLE) {

            CircleShape shape = (CircleShape) fixture.getShape();
            float radius = shape.getRadius();
            vertices[0].set(shape.m_p);
            vertices[0] = Transform.mul(transform, vertices[0]);
            lower.set(vertices[0].x - radius, vertices[0].y - radius);
            upper.set(vertices[0].x + radius, vertices[0].y + radius);

            // define vertices in ccw fashion...
            vertices[0].set(lower.x, lower.y);
            vertices[1].set(upper.x, lower.y);
            vertices[2].set(upper.x, upper.y);
            vertices[3].set(lower.x, upper.y);

            drawSolidPolygon(vertices, 4, AABB_COLOR, true);
        } else if (fixture.getType() == org.jbox2d.collision.shapes.ShapeType.POLYGON) {
            PolygonShape shape = (PolygonShape) fixture.getShape();
            int vertexCount = shape.getVertexCount();

            vertices[0] = shape.getVertex(0);
            vertices[0] = Transform.mul(transform, vertices[0]);
            lower.set(vertices[0]);
            upper.set(lower);
            for (int i = 1; i < vertexCount; i++) {
                vertices[i] = shape.getVertex(i);
                vertices[i] = Transform.mul(transform, vertices[i]);
                lower.x = Math.min(lower.x, vertices[i].x);
                lower.y = Math.min(lower.y, vertices[i].y);
                upper.x = Math.max(upper.x, vertices[i].x);
                upper.y = Math.max(upper.y, vertices[i].y);
            }

            // define vertices in ccw fashion...
            vertices[0].set(lower.x, lower.y);
            vertices[1].set(upper.x, lower.y);
            vertices[2].set(upper.x, upper.y);
            vertices[3].set(lower.x, upper.y);

            drawSolidPolygon(vertices, 4, AABB_COLOR, true);
        }
    }

    private static Vec2 t = new Vec2();
    private static Vec2 axis = new Vec2();

    private void drawShape(Fixture fixture, Transform transform, Color color) {
        if (fixture.getType() == org.jbox2d.collision.shapes.ShapeType.CIRCLE) {
            CircleShape circle = (CircleShape) fixture.getShape();
            t.set(circle.m_p);
            t = Transform.mul(transform, t);
            drawSolidCircle(t, circle.getRadius(), axis.set(transform.q.getCos(), transform.q.getSin()), color);
            return;
        }

        if (fixture.getType() == org.jbox2d.collision.shapes.ShapeType.EDGE) {
            EdgeShape edge = (EdgeShape) fixture.getShape();
            vertices[0] = edge.m_vertex1;
            vertices[1] = edge.m_vertex2;
            vertices[0] = Transform.mul(transform, vertices[0]);
            vertices[1] = Transform.mul(transform, vertices[1]);
            drawSolidPolygon(vertices, 2, color, true);
            return;
        }

        if (fixture.getType() == org.jbox2d.collision.shapes.ShapeType.POLYGON) {
            PolygonShape chain = (PolygonShape) fixture.getShape();
            int vertexCount = chain.getVertexCount();
            for (int i = 0; i < vertexCount; i++) {
                vertices[i] = chain.getVertex(i);
                vertices[i] = Transform.mul(transform, vertices[i]);
            }
            drawSolidPolygon(vertices, vertexCount, color, true);
            return;
        }

        if (fixture.getType() == org.jbox2d.collision.shapes.ShapeType.CHAIN) {
            ChainShape chain = (ChainShape) fixture.getShape();
            int vertexCount = chain.m_count;
            int i = 0;
            for (Vec2 vertex : chain.m_vertices) {
                vertices[i] = vertex;
                vertices[i] = Transform.mul(transform, vertices[i]);
                i++;
            }
            drawSolidPolygon(vertices, vertexCount, color, false);
        }
    }

    private final Vec2 f = new Vec2();
    private final Vec2 v = new Vec2();
    private final Vec2 lv = new Vec2();

    private void drawSolidCircle(Vec2 center, float radius, Vec2 axis, Color color) {
        float angle = 0;
        float angleInc = 2 * (float) Math.PI / 20;
        renderer.setColor(color.r, color.g, color.b, color.a);
        for (int i = 0; i < 20; i++, angle += angleInc) {
            v.set((float) Math.cos(angle) * radius + center.x, (float) Math.sin(angle) * radius + center.y);
            if (i == 0) {
                lv.set(v);
                f.set(v);
                continue;
            }
            renderer.line(lv.x, lv.y, v.x, v.y);
            lv.set(v);
        }
        renderer.line(f.x, f.y, lv.x, lv.y);
        renderer.line(center.x, center.y, 0, center.x + axis.x * radius, center.y + axis.y * radius, 0);
    }

    private void drawSolidPolygon(Vec2[] vertices, int vertexCount, Color color, boolean closed) {
        renderer.setColor(color.r, color.g, color.b, color.a);
        lv.set(vertices[0]);
        f.set(vertices[0]);
        for (int i = 1; i < vertexCount; i++) {
            Vec2 v = vertices[i];
            renderer.line(lv.x, lv.y, v.x, v.y);
            lv.set(v);
        }
        if (closed) renderer.line(f.x, f.y, lv.x, lv.y);
    }

    private void drawJoint(Joint joint) {
        Body bodyA = joint.getBodyA();
        Body bodyB = joint.getBodyB();
        Transform xf1 = bodyA.getTransform();
        Transform xf2 = bodyB.getTransform();

        Vec2 x1 = xf1.p;
        Vec2 x2 = xf2.p;
        Vec2 p1 = new Vec2();
        joint.getAnchorA(p1);
        Vec2 p2 = new Vec2();
        joint.getAnchorB(p2);

        if (JointType.DISTANCE == joint.getType()
                || JointType.CONSTANT_VOLUME == joint.getType()
                || JointType.WELD == joint.getType()) {

            drawSegment(p1, p2, JOINT_COLOR);
        } else if (joint.getType() == JointType.PULLEY) {
            PulleyJoint pulley = (PulleyJoint) joint;
            Vec2 s1 = pulley.getGroundAnchorA();
            Vec2 s2 = pulley.getGroundAnchorB();
            drawSegment(s1, p1, JOINT_COLOR);
            drawSegment(s2, p2, JOINT_COLOR);
            drawSegment(s1, s2, JOINT_COLOR);
        } else if (joint.getType() == JointType.MOUSE) {
            Vec2 anchorA = new Vec2();
            joint.getAnchorA(anchorA);
            Vec2 anchorB = new Vec2();
            joint.getAnchorB(anchorB);
            drawSegment(anchorA, anchorB, JOINT_COLOR);
        } else {
            drawSegment(x1, p1, JOINT_COLOR);
            drawSegment(p1, p2, JOINT_COLOR);
            drawSegment(x2, p2, JOINT_COLOR);
        }
    }

    private void drawSegment(Vec2 x1, Vec2 x2, Color color) {
        renderer.setColor(color);
        renderer.line(x1.x, x1.y, x2.x, x2.y);
    }

    private void drawContact(Contact contact) {
        WorldManifold worldManifold = new WorldManifold();
        contact.getWorldManifold(worldManifold);
        if (worldManifold.points.length == 0) return;
        Vec2 point = worldManifold.points[0];
        renderer.setColor(getColorByBody(contact.getFixtureA().getBody()));
        renderer.point(point.x, point.y, 0);
    }

    private void renderParticles(World world) {
        if (world.getParticleCount() > 0) {

            renderer.begin(ShapeType.Line);

            ParticleColor[] colorBuffer = world.getParticleColorBuffer();
            Vec2[] posBuffer = world.getParticlePositionBuffer();

            for (int i = 0; i < world.getParticleCount(); i++) {
                ParticleColor color = colorBuffer[i];
                Vec2 pos = posBuffer[i];

                renderer.setColor(color.r, color.g, color.b, color.a);
                renderer.circle(pos.x, pos.y, world.getParticleRadius(), 3);
            }
            renderer.end();
        }
    }

    @Override
    public void dispose() {
        renderer.dispose();
    }
}