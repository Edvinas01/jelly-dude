package com.edd.jelly.core.scripts;

import com.badlogic.gdx.Gdx;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public final class ScriptGameContext {

    @Inject
    public ScriptGameContext() {
    }

    public int getFps() {
        if (Gdx.graphics != null) {
            return Gdx.graphics.getFramesPerSecond();
        }
        return -1;
    }
}