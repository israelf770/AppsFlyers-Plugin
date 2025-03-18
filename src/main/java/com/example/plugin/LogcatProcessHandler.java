package com.example.plugin;

import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Key;
import org.jetbrains.annotations.NotNull;
import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.swing.SwingUtilities;

public class LogcatProcessHandler {
    private static final Logger logger = Logger.getInstance(LogcatProcessHandler.class);
    private static String selectedDeviceId = null;
    private static OSProcessHandler currentProcessHandler = null;
    private static String lastLogTimestamp = null;

    public static void setSelectedDeviceId(String deviceId) {
        selectedDeviceId = deviceId;
    }

    public static void resetSelectedDevice() {
        selectedDeviceId = null;
        lastLogTimestamp = null;
    }

    public static void startLogcat() {
        try {
            System.out.println("Selected device: " + selectedDeviceId);
            String adbPath = GetInfo.getAdbPath();
            List<String> devices = GetInfo.getConnectedDevices(adbPath);
            if (devices.isEmpty()) {
                throw new RuntimeException("No ADB devices found");
            }

            SwingUtilities.invokeLater(() -> {
                // נקה את הלוגים הישנים מהממשק לפני שמתחילים
                showLogs.clearAllLogs();
                // נתחיל את הלוגקאט
                startLoggingForDevice(selectedDeviceId, adbPath);
                showLogs.filterLogs(null);
            });
        } catch (Exception e) {
            logger.error("Error starting adb logcat", e);
        }
    }

    private static void startLoggingForDevice(String deviceId, String adbPath) {
        try {
            // סגור את התהליך הקודם אם הוא קיים
            if (currentProcessHandler != null && !currentProcessHandler.isProcessTerminated()) {
                currentProcessHandler.destroyProcess();
            }

            ProcessBuilder builder;

            if (lastLogTimestamp != null) {
                // קריאה מהזמן האחרון שנרשם
                builder = new ProcessBuilder(adbPath, "-s", deviceId, "logcat", "-T", lastLogTimestamp);
                System.out.println("Starting logcat from timestamp: " + lastLogTimestamp);
            } else {
                // נקה את הבאפר של לוגקאט במכשיר כשזו הפעם הראשונה
                clearLogcatBuffer(deviceId, adbPath);

                // קרא את כל הלוגים (בפעם הראשונה בלבד)
                builder = new ProcessBuilder(adbPath, "-s", deviceId, "logcat");
                System.out.println("Starting logcat (first run)");
            }

            Process process = builder.start();
            OSProcessHandler processHandler = getOsProcessHandler(process);
            currentProcessHandler = processHandler;
            processHandler.startNotify();

        } catch (Exception e) {
            logger.error("Error starting logcat for device: " + deviceId, e);
        }
    }

    private static void clearLogcatBuffer(String deviceId, String adbPath) {
        try {
            ProcessBuilder clearBuilder = new ProcessBuilder(adbPath, "-s", deviceId, "logcat", "-c");
            Process clearProcess = clearBuilder.start();
            clearProcess.waitFor(); // נחכה עד שהפקודה תסתיים
            System.out.println("Logcat buffer cleared for device: " + deviceId);
        } catch (Exception e) {
            logger.error("Error clearing logcat buffer for device: " + deviceId, e);
        }
    }

    private static @NotNull OSProcessHandler getOsProcessHandler(Process process) {
        OSProcessHandler processHandler = new OSProcessHandler(process, "adb logcat", StandardCharsets.UTF_8);

        processHandler.addProcessListener(new ProcessAdapter() {
            @Override
            public void onTextAvailable(@NotNull ProcessEvent event, @NotNull Key outputType) {
                String text = event.getText();

                if (text.length() <= 14) return;

                String date = text.substring(0, 18);

                // עדכן את חותמת הזמן האחרונה
                if (date.matches("\\d{4}-\\d{2}-\\d{2}\\s\\d{2}:\\d{2}:\\d{2}.\\d{3}")) {
                    lastLogTimestamp = date;
                }

                if (text.contains("CONVERSION-")) {
                    processLog("CONVERSION", text, date);
                } else if (text.contains("LAUNCH-")) {
                    processLog("LAUNCH", text, date);
                } else if (text.contains("preparing data:")) {
                    processEventLog("EVENT", text, date);
                } else if (text.contains("onDeepLinking")) {
                    processLog("DEEPLINK", text, date);
                    System.out.println(text);
                }
            }
        });

        return processHandler;
    }

    private static void processLog(String type, String text, String date) {
        String formattedLog = LogUtils.extractMessageFromJson(type, text);

        if (text.contains("result:")) {
            int resIndex = text.indexOf("result");
            String shortLog = date + " / " + type + ": " + text.substring(resIndex);
            SwingUtilities.invokeLater(() ->
                showLogs.showUpdateLogs(shortLog,type + ": result", text)
            );
        } else if (formattedLog != null) {
            String shortLog = date + " / " + type + " " + formattedLog;
            SwingUtilities.invokeLater(() ->
                    showLogs.showUpdateLogs(shortLog, type + " " + formattedLog, text)
            );
        }
    }

    private static void processEventLog(String type, String text, String date) {
        String eventInfo = LogUtils.extractMessageFromJson(type, text);

        if (eventInfo != null) {
            SwingUtilities.invokeLater(() -> {
                String logEntry = date + " / " + type + ":\n" + eventInfo;
                showLogs.showUpdateLogs(logEntry, type, text);
            });
        }
    }
}