package com.edd.jelly.core.scripts;

import jdk.nashorn.api.scripting.ScriptObjectMirror;

import java.util.Set;

public final class Script {

    private final String name;
    private final Set<String> returnFunctions;
    private final ScriptObjectMirror returnObject;

    public Script(String name, Set<String> returnFunctions, ScriptObjectMirror returnObject) {
        this.name = name;
        this.returnFunctions = returnFunctions;
        this.returnObject = returnObject;
    }

    public String getName() {
        return name;
    }

    public Set<String> getReturnFunctions() {
        return returnFunctions;
    }

    public ScriptObjectMirror getReturnObject() {
        return returnObject;
    }
}