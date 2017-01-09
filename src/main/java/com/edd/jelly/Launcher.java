package com.edd.jelly;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

public class Launcher extends ApplicationAdapter {

    public static void main(String... args) {
        LwjglApplicationConfiguration configuration =
                new LwjglApplicationConfiguration();

        configuration.foregroundFPS = 60;
        configuration.width = 800;
        configuration.height = 600;

        configuration.fullscreen = false;
        configuration.resizable = false;

        new LwjglApplication(new LiquidFunTest(), configuration);
    }

    @Override
    public void render() {
        System.out.println("running, dt: " + Gdx.graphics.getDeltaTime());
    }
}