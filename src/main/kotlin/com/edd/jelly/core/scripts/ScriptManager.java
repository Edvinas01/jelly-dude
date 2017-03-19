package com.edd.jelly.core.scripts;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import jdk.nashorn.api.scripting.NashornScriptEngine;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.apache.commons.io.monitor.FileAlterationObserver;

import javax.script.*;
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
    private static final String SCRIPT_DIRECTORY = "scripts/";
    private static final String FILE_EXTENSION = ".js";
    private static final String CONTEXT_NAME = "game";

    private final Map<Class<?>, List<?>> hooks = new HashMap<>();
    private Map<String, Script> scripts;

    private final ScriptGameContext context;
    private final NashornScriptEngine engine;

    @Inject
    public ScriptManager(NashornScriptEngine engine,
                         ScriptGameContext context) {

        this.context = context;
        this.engine = engine;
        this.scripts = loadScripts();
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

        if (hookFunctions == null) {
            hookFunctions = new ArrayList<>();
            hooks.put(hookType, hookFunctions);
        }
        hookFunctions.addAll(getHookFunctions(hookType));
        return Collections.unmodifiableList(hookFunctions);
    }

    /**
     * Create file change observer for scripts.
     */
    public FileAlterationObserver createObserver() {
        FileAlterationObserver observer = new FileAlterationObserver(SCRIPT_DIRECTORY);
        observer.addListener(new ScriptListener(this));
        return observer;
    }

    /**
     * Check if file is a script.
     */
    boolean isScript(File file) {
        return file.getPath().endsWith(FILE_EXTENSION);
    }

    /**
     * Reload all loaded scripts and hooks.
     */
    void reloadScripts() {

        // Load new scripts.
        scripts = loadScripts();

        // Reload hooks according to new scripts.
        hooks.forEach((type, functions) -> {
            functions.clear();

            //noinspection unchecked
            ((List<Object>) functions).addAll(getHookFunctions(type));
        });
    }

    /**
     * Get hook functions for a hook type from scripts.
     */
    private <T> List<T> getHookFunctions(Class<T> hookType) {

        // Function names which are required to support this hook.
        Set<String> requiredFunctions = Stream
                .of(hookType.getMethods())
                .map(Method::getName)
                .collect(Collectors.toSet());

        if (requiredFunctions.isEmpty()) {
            return Collections.emptyList();
        }

        // Fin scripts, which are applicable for this hook.
        return scripts.values()
                .stream()
                .filter(s -> s.getReturnFunctions().containsAll(requiredFunctions))
                .map(s -> engine.getInterface(s.getReturnObject(), hookType))
                .collect(Collectors.toList());
    }

    /**
     * Load all scripts from script directory.
     */
    private Map<String, Script> loadScripts() {
        try {
            return Files.walk(Paths.get(SCRIPT_DIRECTORY))
                    .map(Path::toFile)
                    .filter(this::isScript)
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

            // Make sure that script context is always new for each script.
            engine.setContext(createContext());

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

    /**
     * Create a fresh script context.
     */
    private SimpleScriptContext createContext() {
        SimpleScriptContext ctx = new SimpleScriptContext();
        ctx.setAttribute(CONTEXT_NAME, context, ScriptContext.ENGINE_SCOPE);
        return ctx;
    }
}