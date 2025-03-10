package com.example.plugin;

    import com.intellij.execution.process.OSProcessHandler;
    import com.intellij.execution.process.ProcessAdapter;
    import com.intellij.execution.process.ProcessEvent;
    import com.intellij.openapi.diagnostic.Logger;
    import com.intellij.openapi.util.Key;
    import org.jetbrains.annotations.NotNull;

    import javax.swing.*;
    import java.io.BufferedReader;
    import java.io.IOException;
    import java.io.InputStreamReader;
    import java.nio.charset.StandardCharsets;
    import java.util.ArrayList;
    import java.util.List;

    public class LogcatProcessHandler {
        private static final Logger logger = Logger.getInstance(LogcatProcessHandler.class);
        private static String selectedDeviceId = null;

        public static void setSelectedDeviceId(String deviceId) {
            selectedDeviceId = deviceId;
        }
        public static void resetSelectedDevice() {
            selectedDeviceId = null;
        }

        public static void startLogcat() {
            try {
                String adbPath = GetInfo.getAdbPath();
                List<String> devices = GetInfo.getConnectedDevices(adbPath);

                if (devices.isEmpty()) {
                    throw new RuntimeException("No ADB devices found");
                }

                if (selectedDeviceId == null) {
                    SwingUtilities.invokeLater(() -> DeviceSelector.showDeviceSelector(devices, adbPath));
                } else {
                    SwingUtilities.invokeLater(() -> startLoggingForDevice(selectedDeviceId, adbPath));
                }

            } catch (Exception e) {
                logger.error("Error starting adb logcat", e);
            }
        }

        private static void startLoggingForDevice(String deviceId, String adbPath) {
            try {
                ProcessBuilder builder = new ProcessBuilder(adbPath, "-s", deviceId, "logcat");
                Process process = builder.start();
                OSProcessHandler processHandler = getOsProcessHandler(process);
                processHandler.startNotify();

                if (LogPopup.getPopup() == null) {
                    LogPopup.createPopup();
                }
            } catch (Exception e) {
                logger.error("Error starting logcat for device: " + deviceId, e);
            }
        }

        private static @NotNull OSProcessHandler getOsProcessHandler(Process process) {
            OSProcessHandler processHandler = new OSProcessHandler(process, "adb logcat", StandardCharsets.UTF_8);

            processHandler.addProcessListener(new ProcessAdapter() {
                @Override
                public void onTextAvailable(@NotNull ProcessEvent event, @NotNull Key outputType) {
                    String text = event.getText();

                    if (text.length() <= 14) return;

                    String date = text.substring(0, 14);

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
