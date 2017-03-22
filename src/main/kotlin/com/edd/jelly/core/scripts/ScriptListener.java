package com.edd.jelly.core.scripts;

import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

public final class ScriptListener extends FileAlterationListenerAdaptor {

    private static final Logger LOG = LogManager.getLogger(ScriptListener.class);
    private final ScriptManager manager;

    ScriptListener(ScriptManager manager) {
        this.manager = manager;
    }

    @Override
    public void onFileCreate(File file) {
        if (manager.isScript(file)) {
            LOG.trace("{} created", file.getPath());
            manager.reloadScripts();
        }
    }

    @Override
    public void onFileChange(File file) {
        if (manager.isScript(file)) {
            LOG.trace("{} modified", file.getPath());
            manager.reloadScripts();
        }
    }

    @Override
    public void onFileDelete(File file) {
        if (manager.isScript(file)) {
            LOG.trace("{} deleted", file.getPath());
            manager.reloadScripts();
        }
    }
}