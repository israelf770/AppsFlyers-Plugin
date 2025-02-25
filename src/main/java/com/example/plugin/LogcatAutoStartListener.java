package com.example.plugin;

import com.intellij.execution.ExecutionListener;
import com.intellij.execution.ExecutionManager;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import org.jetbrains.annotations.NotNull;

public class LogcatAutoStartListener implements StartupActivity.DumbAware {
    @Override
    public void runActivity(@NotNull Project project) {
        project.getMessageBus().connect().subscribe(ExecutionManager.EXECUTION_TOPIC, new ExecutionListener() {
            @Override
            public void processStarted(@NotNull String executorId, @NotNull ExecutionEnvironment env, @NotNull ProcessHandler handler) {
                if (env.getRunProfile().getName().toLowerCase().contains("android")) {
                    LogcatProcessHandler.startLogcat();
                }
            }
        });
    }
}
