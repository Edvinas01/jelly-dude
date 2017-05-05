package com.edd.jelly.core.scripts;

import jdk.nashorn.api.scripting.NashornException;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

/**
 * Wrapper class for executing hooks.
 */
public final class Hook<T> {

    private final List<T> functions = new ArrayList<>();

    private final Class<T> hookType;
    private final Logger logger;

    private boolean flagged = false;

    Hook(Class<T> hookType, Logger logger) {
        this.hookType = hookType;
        this.logger = logger;
    }

    /**
     * Add additional hook functions to the hook.
     */
    void addFunctions(Collection<T> functions) {
        this.functions.addAll(functions);
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
        } catch (NashornException e) {
            logger.error("Could not execute hook function for hook of type: {}",
                    hookType.getSimpleName(), e);

            flagged = true;
        }
    }
}