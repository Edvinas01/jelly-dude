package com.edd.jelly.core.scripts;

import jdk.nashorn.api.scripting.ScriptObjectMirror;

import java.util.Set;

final class Script {

    private final String name;
    private final Set<String> returnFunctions;
    private final ScriptObjectMirror returnObject;

    Script(String name,
           Set<String> returnFunctions,
           ScriptObjectMirror returnObject) {

        this.name = name;
        this.returnFunctions = returnFunctions;
        this.returnObject = returnObject;
    }

    String getName() {
        return name;
    }

    Set<String> getReturnFunctions() {
        return returnFunctions;
    }

    ScriptObjectMirror getReturnObject() {
        return returnObject;
    }
}