package com.example.plugin;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public class StartLogcatAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        // קריאה למתודה startLogcat() כדי להתחיל את ההאזנה ל-Logcat
        LogcatProcessHandler.startLogcat();
    }
}
