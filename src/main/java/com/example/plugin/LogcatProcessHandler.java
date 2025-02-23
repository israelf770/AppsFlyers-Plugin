package com.example.plugin;

import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.openapi.util.Key;

import java.nio.charset.StandardCharsets;

public class LogcatProcessHandler {
    private static final Logger logger = Logger.getInstance(LogcatProcessHandler.class);

    public static void startLogcat() {
        try {
            logger.info("Logcat listener started");
            ProcessBuilder builder = new ProcessBuilder("adb", "logcat", "*:V");
            Process process = builder.start();
            OSProcessHandler processHandler = getOsProcessHandler(process);
            processHandler.startNotify();
        } catch (Exception e) {
            logger.error("Error starting adb logcat", e);
        }
    }

    private static @NotNull OSProcessHandler getOsProcessHandler(Process process) {
        OSProcessHandler processHandler = new OSProcessHandler(process, "adb logcat", StandardCharsets.UTF_8);
        processHandler.addProcessListener(new ProcessAdapter() {
            @Override
            public void onTextAvailable(@NotNull ProcessEvent event, @NotNull Key outputType) {
                String text = event.getText();
                if (text.contains("CONVERSION-") || text.contains("LAUNCH-")) {
                    String formattedLog = LogUtils.extractKeyValueFromLog(text);
                    if (formattedLog != null) {
                        LogPopup.showPopup(formattedLog);
                    }
                }
            }
        });
        return processHandler;
    }
}
