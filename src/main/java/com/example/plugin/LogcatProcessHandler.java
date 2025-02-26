package com.example.plugin;

import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.openapi.util.Key;
import javax.swing.SwingUtilities;

import java.nio.charset.StandardCharsets;

public class LogcatProcessHandler {
    private static final Logger logger = Logger.getInstance(LogcatProcessHandler.class);

    public static void startLogcat() {
        try {
            LogPopup.getDisplayedLogs().clear();
            if (LogPopup.getPopup() != null && LogPopup.getPopup().isVisible()) {
                LogPopup.getPopup().dispose();
                LogPopup.setPopup(null);
            }
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
                if (text.contains("CONVERSION-")&&text.length()>14) {
                    String formattedLog = LogUtils.extractKeyValueFromLog(text);
                    String date = text.substring(0, 14);
                    if (formattedLog != null) {
                        SwingUtilities.invokeLater(() -> LogPopup.showPopup(date + "/ CONVERSION: " + formattedLog));
                    }
                    if(text.contains("result:")){
                        int resIndex = text.indexOf("result");
                        SwingUtilities.invokeLater(() -> LogPopup.showPopup(date + "/ CONVERSION: " + text.substring(resIndex)));
                    }
                }
                if (text.contains("LAUNCH-")&&text.length()>14) {
                    String formattedLog = LogUtils.extractKeyValueFromLog(text);
                    String date = text.substring(0, 14);
                    if (text.contains("new task added: LAUNCH")){
                        SwingUtilities.invokeLater(() -> LogPopup.showPopup("new task added: LAUNCH"));
                    }
                    if(text.contains("result:")){
                        int resIndex = text.indexOf("result");
                        SwingUtilities.invokeLater(() -> LogPopup.showPopup(date + "/ LAUNCH: " + text.substring(resIndex)));
                    }
                    if (formattedLog != null) {
                        SwingUtilities.invokeLater(() -> LogPopup.showPopup(date + "/ LAUNCH " + formattedLog));
                    }
                }
            }
        });
        return processHandler;
    }
}
