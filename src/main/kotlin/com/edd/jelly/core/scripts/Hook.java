package com.edd.jelly.core.scripts;

import jdk.nashorn.api.scripting.NashornException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

/**
 * Wrapper class for executing hooks.
 */
public final class Hook<T> {

    private static final Logger LOG = LogManager.getLogger(Hook.class);

    private final List<T> functions = new ArrayList<>();

    private final Class<T> hookType;

    private boolean flagged = false;

    Hook(Class<T> hookType) {
        this.hookType = hookType;
    }

    /**
     * Add additional hook functions to the hook.
     */
    void addFunctions(Collection<T> functions) {
        functions.forEach(f -> {
            LOG.debug("Adding function: {}", f.getClass().getSimpleName());
            this.functions.add(f);
        });
    }

    /**
     * Replace existing hook function list with new functions, this also resets the hook flag.
     */
    void setFunctions(Collection<T> functions) {
        flagged = false;

        this.functions.clear();
        this.functions.addAll(functions);
    }

    /**
     * Run the hook by providing a consumer which accepts the hook function.
     */
    public void run(Consumer<T> consumer) {
        if (flagged) {
            return;
        }

        try {
            functions.forEach(consumer);
        } catch (RuntimeException e) {
            LOG.error("Could not execute hook function for hook of type: {}",
                    hookType.getSimpleName(), e);

            flagged = true;
        }
    }
}