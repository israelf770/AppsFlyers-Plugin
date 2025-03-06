package com.example.plugin;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class StartLogcatAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if(LogPopup.getPopup()==null){
            LogcatProcessHandler.startLogcat(project);
        } else {
            LogUtils.clearLogs();
            LogcatProcessHandler.startLogcat(project);
        }

    }
}
