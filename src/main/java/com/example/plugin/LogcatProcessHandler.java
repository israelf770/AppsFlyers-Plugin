package com.example.plugin;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.ui.Gray;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBScrollPane;
import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.openapi.util.Key;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;  // Changed from java.awt.List
import java.util.Map;
import java.util.HashMap;  // Added missing import

public class LogcatProcessHandler {
    private static final Logger logger = Logger.getInstance(LogcatProcessHandler.class);
    private static String selectedDeviceId = null;

    // Add this method to reset device selection
    public static void resetSelectedDevice() {
        selectedDeviceId = null;
    }
    public static void startLogcat() {
        try {
            String adbPath = getAdbPath();
            List<String> devices = getConnectedDevices(adbPath);

            if (devices.isEmpty()) {
                throw new RuntimeException("No ADB devices found");
            }

            // Only show device selector if no device is selected
            if (selectedDeviceId == null) {
                SwingUtilities.invokeLater(() -> {
                    showDeviceSelector(devices, adbPath);
                });
            } else {
                // Start logging with the selected device
                SwingUtilities.invokeLater(() -> {
                    startLoggingForDevice(selectedDeviceId, adbPath);
                });
            }

        } catch (Exception e) {
            logger.error("Error starting adb logcat", e);
        }
    }

    // Add this method to start logging for a specific device
    private static void startLoggingForDevice(String deviceId, String adbPath) {
        try {
            ProcessBuilder builder = new ProcessBuilder(adbPath, "-s", deviceId, "logcat");
            Process process = builder.start();
            OSProcessHandler processHandler = getOsProcessHandler(process);
            processHandler.startNotify();

            // Create the popup window if it doesn't exist
            if (LogPopup.getPopup() == null) {
                LogPopup.createPopup();
            }
        } catch (Exception e) {
            logger.error("Error starting logcat for device: " + deviceId, e);
        }
    }

    private static List<String> getConnectedDevices(String adbPath) throws IOException {
        List<String> devices = new ArrayList<>();  // Now using java.util.ArrayList
        ProcessBuilder builder = new ProcessBuilder(adbPath, "devices");
        Process process = builder.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            reader.readLine(); // Skip header

            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    String[] parts = line.split("\\s+");
                    if (parts.length >= 2) {
                        devices.add(parts[0]);
                    }
                }
            }
        }

        return devices;
    }

    private static void showDeviceSelector(List<String> devices, String adbPath) {
        try {
            // Clear existing logs when showing device selector
            LogPopup.getDisplayedLogs().clear();
            if (LogPopup.getPopup() != null) {
                LogPopup.getPopup().dispose();
                LogPopup.setPopup(null);
            }
            JDialog dialog = new JDialog((Frame) null, "ADB Device Manager", true);
            dialog.setLayout(new BorderLayout(10, 10));
            dialog.getRootPane().setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
            mainPanel.setBackground(new JBColor(new Color(246, 241, 241), Gray._50));

            // Create header label
            JLabel headerLabel = new JLabel("Select Android Device");
            headerLabel.setFont(new Font("Arial", Font.BOLD, 16));
            headerLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
            mainPanel.add(headerLabel, BorderLayout.NORTH);

            // Create device list panel
            JPanel deviceListPanel = new JPanel();
            deviceListPanel.setLayout(new BoxLayout(deviceListPanel, BoxLayout.Y_AXIS));
            deviceListPanel.setBackground(new JBColor(Gray._255, Gray._43));
            deviceListPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            ButtonGroup group = new ButtonGroup();
            Map<JRadioButton, String> buttonToDevice = new HashMap<>();

            // Create buttons first
            JButton refreshButton = new JButton("Refresh");
            JButton okButton = new JButton("Connect");
            JButton cancelButton = new JButton("Cancel");

            // Initially disable the Connect button
            okButton.setEnabled(false);

            // Add devices to the list
            for (String deviceId : devices) {
                String deviceInfo = getDeviceInfo(adbPath, deviceId);
                JPanel devicePanel = createDevicePanel(deviceId, deviceInfo, group, buttonToDevice, okButton);
                deviceListPanel.add(devicePanel);
                deviceListPanel.add(Box.createVerticalStrut(5));
            }

            JScrollPane scrollPane = new JBScrollPane(deviceListPanel);
            scrollPane.setPreferredSize(new Dimension(400, 200));
            mainPanel.add(scrollPane, BorderLayout.CENTER);

            JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            buttonsPanel.setBackground(mainPanel.getBackground());

            // Add button actions
            okButton.addActionListener(e -> {
                for (Map.Entry<JRadioButton, String> entry : buttonToDevice.entrySet()) {
                    if (entry.getKey().isSelected()) {
                        selectedDeviceId = entry.getValue();
                        dialog.dispose();
                        startLogcat();
                        return;
                    }
                }
            });

            refreshButton.addActionListener(e -> {
                dialog.dispose();
                startLogcat();
            });

            cancelButton.addActionListener(e -> dialog.dispose());

            // Add buttons to panel
            buttonsPanel.add(refreshButton);
            buttonsPanel.add(Box.createHorizontalStrut(5));
            buttonsPanel.add(okButton);
            buttonsPanel.add(Box.createHorizontalStrut(5));
            buttonsPanel.add(cancelButton);

            mainPanel.add(buttonsPanel, BorderLayout.SOUTH);
            dialog.add(mainPanel);
            dialog.pack();
            dialog.setLocationRelativeTo(null);
            dialog.setVisible(true);

        } catch (Exception e) {
            logger.error("Error showing device selector", e);
        }
    }

    private static JPanel createDevicePanel(String deviceId, String deviceInfo, ButtonGroup group,
                                            Map<JRadioButton, String> buttonToDevice, JButton okButton) {
        JPanel devicePanel = new JPanel(new BorderLayout(10, 0));
        devicePanel.setBackground(new JBColor(Gray._255, Gray._43));
        devicePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, JBColor.border()),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        JRadioButton radio = new JRadioButton("Select");
        radio.setBackground(devicePanel.getBackground());
        radio.setFont(new Font("Arial", Font.PLAIN, 12));
        group.add(radio);
        buttonToDevice.put(radio, deviceId);

        // Add action listener directly to radio button
        radio.addActionListener(e -> okButton.setEnabled(radio.isSelected()));

        JPanel infoPanel = new JPanel(new GridLayout(2, 1));
        infoPanel.setBackground(devicePanel.getBackground());

        JLabel nameLabel = new JLabel(deviceInfo);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 12));

        JLabel idLabel = new JLabel("ID: " + deviceId);
        idLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        idLabel.setForeground(JBColor.GRAY);

        infoPanel.add(nameLabel);
        infoPanel.add(idLabel);

        devicePanel.add(radio, BorderLayout.WEST);
        devicePanel.add(infoPanel, BorderLayout.CENTER);

        // Make entire panel clickable
        devicePanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                radio.setSelected(true);
                okButton.setEnabled(true);
            }
        });

        return devicePanel;
    }

    private static String getDeviceInfo(String adbPath, String deviceId) {
        try {
            ProcessBuilder builder = new ProcessBuilder(adbPath, "-s", deviceId, "shell", "getprop", "ro.product.model");
            Process process = builder.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String model = reader.readLine();
                return deviceId + " (" + (model != null ? model : "Unknown") + ")";
            }
        } catch (IOException e) {
            return deviceId + " (Unknown)";
        }
    }

    private static String getAdbPath() {
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            return System.getProperty("user.home") + "\\AppData\\Local\\Android\\Sdk\\platform-tools\\adb.exe";
        } else if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            return "/Users/" + System.getProperty("user.name") + "/Library/Android/sdk/platform-tools/adb";
        } else {
            throw new RuntimeException("Unsupported OS");
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

                if (text.contains("preparing data:")) {
                    System.out.println("Debug: Found preparing data in text: " + text);
                }

                // Handle LAUNCH logs
                 if (text.contains("LAUNCH-")) {
                    processLog("LAUNCH", text, date);

                }
                // Handle EVENT logs - new addition
                 else if (text.contains("preparing data:")) {
                     System.out.println("Debug: Processing event log");
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

        String formattedLog = LogUtils.extractKeyValueFromLog(text);

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
        // Extract everything after "preparing data:"
        int startIndex = text.indexOf("preparing data:");
        if (startIndex != -1) {
            String eventData = text.substring(startIndex + "preparing data:".length()).trim();
            System.out.println("Debug: Event data: " + eventData);

            // Show the raw event data in the popup
            SwingUtilities.invokeLater(() -> {
                String logEntry = date + " / " + type + ":\n" + eventData;
                System.out.println("Debug: Showing popup with: " + logEntry);
                LogPopup.showPopup(logEntry);
            });
        }
    }
}
