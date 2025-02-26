package com.example.plugin;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public class StartLogcatAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        if(LogPopup.getPopup()==null){
            LogcatProcessHandler.startLogcat();
        } else {
            LogUtils.clearLogs();
            LogcatProcessHandler.startLogcat();
        }

    }
}
