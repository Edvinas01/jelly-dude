package com.edd.jelly.core.scripts;

import com.badlogic.gdx.Gdx;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.HashMap;
import java.util.Map;

@Singleton
@SuppressWarnings("unused")
public final class ScriptGameContext {

    private final Map<String, Object> stored = new HashMap<>();

    @Inject
    public ScriptGameContext() {
    }

    public Object get(String name) {
        return stored.get(name);
    }

    public void set(String name, Object value) {
        stored.put(name, value);
    }


    public void clear() {
        stored.clear();
    }

    public void clear(String name) {
        stored.remove(name);
    }

    public float getDeltaTime() {
        return Gdx.graphics.getDeltaTime();
    }

    public int getFps() {
        return Gdx.graphics.getFramesPerSecond();
    }
}