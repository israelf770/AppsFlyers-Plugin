package com.example.plugin;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class StartLogcatAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        LogcatProcessHandler.resetSelectedDevice();
        LogcatProcessHandler.startLogcat();

    }
}