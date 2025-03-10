package com.example.plugin;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public class StartLogcatAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        if (LogPopup.getPopup() == null) {
            // If no popup exists, start new session
            startNewSession();
        } else {
            // If popup exists, just clear logs and continue with current device
            LogUtils.clearLogs();
            startNewSession();
        }
    }

    private void startNewSession() {
        // Initialize with null device ID to force first device selection
        if (LogPopup.getPopup() == null) {
            LogcatProcessHandler.resetSelectedDevice();
        }
        LogcatProcessHandler.startLogcat();
    }
}