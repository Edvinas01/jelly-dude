package com.edd.jelly.core.scripts;

import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;

import java.io.File;

// TODO logging for scripts
public final class ScriptListener extends FileAlterationListenerAdaptor {

    private final ScriptManager manager;

    ScriptListener(ScriptManager manager) {
        this.manager = manager;
    }

    @Override
    public void onFileCreate(File file) {
        if (manager.isScript(file)) {

            System.out.printf("New script created: %s\n", file.getPath());
            manager.reloadScripts();
        }
    }

    @Override
    public void onFileChange(File file) {
        if (manager.isScript(file)) {

            System.out.printf("Modified script: %s\n", file.getPath());
            manager.reloadScripts();
        }
    }

    @Override
    public void onFileDelete(File file) {
        if (manager.isScript(file)) {

            System.out.printf("Deleted script: %s\n", file.getPath());
            manager.reloadScripts();
        }
    }
}