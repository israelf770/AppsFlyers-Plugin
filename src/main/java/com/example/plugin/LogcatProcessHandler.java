package com.example.plugin;
import com.example.plugin.LogUtils;
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
            String adbPath;
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                adbPath = System.getProperty("user.home") + "\\AppData\\Local\\Android\\Sdk\\platform-tools\\adb.exe";
            } else if (System.getProperty("os.name").toLowerCase().contains("mac")) {
                adbPath = "/Users/" + System.getProperty("user.name") + "/Library/Android/sdk/platform-tools/adb";
            } else {
                throw new RuntimeException("Unsupported OS");
            }

            logger.info("Logcat listener started");
            ProcessBuilder builder = new ProcessBuilder(adbPath, "logcat", "*:V");
            Process process = builder.start();
            OSProcessHandler processHandler = getOsProcessHandler(process);
            processHandler.startNotify();

        } catch (Exception e) {
            logger.error("Error starting adb logcat", e);
        }
    }

    private static final int DATE_LENGTH = 14;

    private static @NotNull OSProcessHandler getOsProcessHandler(Process process) {
        OSProcessHandler processHandler = new OSProcessHandler(process, "adb logcat", StandardCharsets.UTF_8);

        processHandler.addProcessListener(new ProcessAdapter() {
            @Override
            public void onTextAvailable(@NotNull ProcessEvent event, @NotNull Key outputType) {
                String text = event.getText();
                if (text.length() <= DATE_LENGTH) return;

                String date = text.substring(0, DATE_LENGTH);

                // Check for different log types
                if (text.contains("CONVERSION-")) {
                    processLog("CONVERSION", text, date);
                } else if (text.contains("LAUNCH-")) {
                    processLog("LAUNCH", text, date);
                } else if (text.contains("INAPP-")) {
                    processLog("IN-APP EVENT", text, date);
                }
            }
        });

        return processHandler;
    }

    private static void processLog(String type, String text, String date) {
        // Handle different log types
        if ("LAUNCH".equals(type) && text.contains("new task added: LAUNCH")) {
            SwingUtilities.invokeLater(() -> LogPopup.showPopup("new task added: LAUNCH"));
            return;
        }

        // For in-app events, use the event extraction method
        if ("IN-APP EVENT".equals(type)) {
            String formattedEvent = LogUtils.extractEventFromLog(text);
            if (formattedEvent != null) {
                SwingUtilities.invokeLater(() -> LogPopup.showPopup(date + " / " + type + ":\n" + formattedEvent));
                return;
            }
        }

        // For other log types, try to extract key value
        String formattedLog = LogUtils.extractEventFromLog (text);
        if (text.contains("result:")) {
            int resIndex = text.indexOf("result");
            SwingUtilities.invokeLater(() -> LogPopup.showPopup(date + " / " + type + ": " + text.substring(resIndex)));
        } else if (formattedLog != null && !formattedLog.isEmpty()) {
            SwingUtilities.invokeLater(() -> LogPopup.showPopup(date + " / " + type + " " + formattedLog));
        }
    }
}
