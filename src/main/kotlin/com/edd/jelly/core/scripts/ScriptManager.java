package com.edd.jelly.core.scripts;

import com.edd.jelly.util.Configuration;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import jdk.nashorn.api.scripting.NashornScriptEngine;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.script.*;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Singleton
public final class ScriptManager {

    private static final Logger LOG = LogManager.getLogger(ScriptManager.class);

    private static final String MAIN_FUNCTION_NAME = "main";
    private static final String SCRIPT_DIRECTORY = Configuration.ASSETS_FOLDER + "scripts/";
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

            LOG.debug("Registering hook of type: {}", hookType.getSimpleName());
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
        LOG.debug("Reloading scripts and hooks");

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
            return Files.walk(getScriptDirectory().toPath())
                    .map(Path::toFile)
                    .filter(this::isScript)
                    .map(this::loadScript)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toMap(Script::getName, s -> s));

        } catch (IOException e) {
            LOG.error("Could not load scripts", e);

            return Collections.emptyMap();
        }
    }

    /**
     * Load and compile script file contents.
     */
    private Optional<Script> loadScript(File script) {
        String name = script.getPath();
        try (FileReader reader = new FileReader(script)) {

            // Make sure that script context is always new for each script.
            engine.setContext(createContext());

            return runMain(engine
                    .compile(reader))
                    .map(m -> {
                        LOG.debug("{} loaded", name);
                        return new Script(name, m.keySet(), m);
                    });

        } catch (IOException e) {
            LOG.error("{} could not be loaded", name, e);
        } catch (ScriptException e) {
            LOG.error("{} could not be executed:\n {}", name, e.getMessage());
        } catch (NoSuchMethodException e) {
            LOG.debug("{} doesn't have a {}() function", name, MAIN_FUNCTION_NAME);
        }
        return Optional.empty();
    }

    /**
     * Attempt to run the main function of a script.
     */
    private Optional<ScriptObjectMirror> runMain(CompiledScript script)
            throws ScriptException, NoSuchMethodException {

        script.eval();
        Object result = engine.invokeFunction(MAIN_FUNCTION_NAME);

        if (result instanceof ScriptObjectMirror) {
            return Optional.of((ScriptObjectMirror) result);
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

    /**
     * Get script directory, if it doesn't exist, it will get created.
     */
    private File getScriptDirectory() {
        File scripts = new File(SCRIPT_DIRECTORY);
        if (!scripts.exists() || scripts.isFile()) {

            //noinspection ResultOfMethodCallIgnored
            scripts.mkdir();
        }
        return scripts;
    }
}