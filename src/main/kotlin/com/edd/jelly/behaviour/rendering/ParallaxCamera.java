package com.edd.jelly.behaviour.rendering;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

public class ParallaxCamera extends OrthographicCamera {

    private final Matrix4 parallaxCombined = new Matrix4();
    private final Matrix4 parallaxView = new Matrix4();

    private final Vector3 temporary = new Vector3();
    private final Vector3 temporaryTwo = new Vector3();

    public ParallaxCamera(float viewportWidth, float viewportHeight) {
        super(viewportWidth, viewportHeight);
    }

    public Matrix4 calculateParallaxMatrix(float parallaxX,
                                           float parallaxY) {

        update();

        temporary.set(position);
        temporary.x *= parallaxX;
        temporary.y *= parallaxY;

        parallaxView.setToLookAt(temporary, temporaryTwo.set(temporary).add(direction), up);
        parallaxCombined.set(projection);

        Matrix4.mul(parallaxCombined.val, parallaxView.val);
        return parallaxCombined;
    }
}