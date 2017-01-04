package com.edd.jelly.exception;

import java.text.MessageFormat;

/**
 * Base game exception.
 */
public class JellyException extends RuntimeException {

    public JellyException(String message) {
        super(message);
    }

    public JellyException(String pattern, Object... args) {
        super(MessageFormat.format(pattern, args));
    }
}