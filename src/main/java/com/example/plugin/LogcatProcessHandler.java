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

                if (text.length() <= DATE_LENGTH) return; // Ignore lines that are too short

                String date = text.substring(0, DATE_LENGTH);

                 if (text.contains("CONVERSION-")) {
                     processLog("CONVERSION", text, date);
                 }

                 if (text.contains("LAUNCH-")) {
                    processLog("LAUNCH", text, date);
                }
                // Handle EVENT logs - new addition
                 else if (text.contains("preparing data:")) {
                     processEventLog("EVENT", text, date);
                 }
            }
        });

        return processHandler;
    }

    private static void processLog(String type, String text, String date) {

        if ("LAUNCH".equals(type) && text.contains("new task added: LAUNCH")) {
            SwingUtilities.invokeLater(() -> LogPopup.showPopup("new task added: LAUNCH"));
            return;
        }

        String formattedLog = LogUtils.extractKeyValueFromLog(type,text);

        if (text.contains("result:")) {
            int resIndex = text.indexOf("result");
            SwingUtilities.invokeLater(() -> LogPopup.showPopup(date + " / " + type + ": " + text.substring(resIndex)));
        }else if (formattedLog != null) {
            String finalFormattedLog = formattedLog;
            SwingUtilities.invokeLater(() -> LogPopup.showPopup(date + " / " + type + " " + finalFormattedLog));
        }
    }
    // New method to process event logs
    private static void processEventLog(String type, String text, String date) {
        String eventData = LogUtils.extractKeyValueFromLog(type, text);

        if (eventData != null) {
            System.out.println("Event Data: " + eventData);
           SwingUtilities.invokeLater(() -> {
               String logEntry = date + " / " + type + ":\n" + eventData;
               LogPopup.showPopup(logEntry);
           });
        }
    }
}
