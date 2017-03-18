package com.edd.jelly.core.scripts;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import jdk.nashorn.api.scripting.NashornScriptEngine;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.apache.commons.io.monitor.FileAlterationMonitor;

import javax.script.CompiledScript;
import javax.script.ScriptException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Singleton
public final class ScriptManager {

    private static final String MAIN_FUNCTION_NAME = "main";
    private static final String FILE_EXTENSION = ".js";
    private static final String SCRIPT_DIRECTORY = "scripts/";

    private final FileAlterationMonitor monitor;
    private final NashornScriptEngine engine;

    private final Map<Class<?>, List<?>> hooks = new HashMap<>();
    private Map<String, Script> scripts;

    @Inject
    public ScriptManager(FileAlterationMonitor monitor,
                         NashornScriptEngine engine) {

        this.monitor = monitor;
        this.engine = engine;

        this.scripts = loadScripts();
        System.out.println();
    }

    /**
     * Get list of hook functions for a hook type.
     *
     * @param hookType hook type whose functions to get.
     * @param <T>      hook type.
     * @return iterable of hooks, never null.
     */
    public <T> Iterable<T> hook(Class<T> hookType) {

        @SuppressWarnings("unchecked")
        List<T> hookFunctions = (List<T>) hooks.get(hookType);

        // Lazy hook function initialization.
        if (hookFunctions == null) {
            hookFunctions = new ArrayList<>();
            hooks.put(hookType, hookFunctions);
        }

        // Function names which are required to support this hook.
        Set<String> requiredFunctions = Stream
                .of(hookType.getMethods())
                .map(Method::getName)
                .collect(Collectors.toSet());

        // Register all hooks from scripts, which are applicable for these hooks.
        hookFunctions.addAll(scripts.values()
                .stream()
                .filter(s -> s.getReturnFunctions().containsAll(requiredFunctions))
                .map(s -> engine.getInterface(s.getReturnObject(), hookType))
                .collect(Collectors.toList()));

        return Collections.unmodifiableList(hookFunctions);
    }

    /**
     * Load all scripts from script directory.
     */
    public Map<String, Script> loadScripts() {
        try {
            return Files.walk(Paths.get(SCRIPT_DIRECTORY))
                    .map(Path::toFile)
                    .filter(f -> f.getPath().endsWith(FILE_EXTENSION))
                    .map(this::loadScript)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toMap(Script::getName, s -> s));

        } catch (IOException e) {
            e.printStackTrace(); // TODO logging

            return Collections.emptyMap();
        }
    }

    /**
     * Load and compile script file contents.
     */
    private Optional<Script> loadScript(File script) {
        try (FileReader reader = new FileReader(script)) {
            return runMain(engine.compile(reader))
                    .map(m -> new Script(script.getPath(), m.keySet(), m));

        } catch (IOException | ScriptException e) {
            e.printStackTrace(); // TODO logging
        }
        return Optional.empty();
    }

    /**
     * Attempt to run the main function of a script.
     */
    private Optional<ScriptObjectMirror> runMain(CompiledScript script) {
        try {
            script.eval();
            Object result = engine.invokeFunction(MAIN_FUNCTION_NAME);

            if (result instanceof ScriptObjectMirror) {
                return Optional.of((ScriptObjectMirror) result);
            }
        } catch (ScriptException | NoSuchMethodException e) {
            e.printStackTrace(); // TODO logging
        }
        return Optional.empty();
    }
}