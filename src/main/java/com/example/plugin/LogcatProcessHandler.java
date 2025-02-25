package com.example.plugin;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonParseException;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.util.Key;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextArea;
import org.jetbrains.annotations.NotNull;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;


import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogcatProcessHandler {
    private static final Logger logger = Logger.getInstance(LogcatProcessHandler.class);

    // These static collections store the logs that have been displayed.
    // We'll clear them at the start of a new run.
    private static final Set<String> displayedLogs = new HashSet<>();
    private static final DefaultListModel<String> logListModel = new DefaultListModel<>();

    // Static popup and its components.
    private static JBPopup popup;
    private static JPanel logPanel;
    private static JScrollPane scrollPane;

    // Color and font constants for UI styling.
    private static final JBColor BACKGROUND_COLOR = new JBColor(new Color(30, 30, 30), new Color(30, 30, 30));
    private static final JBColor ENTRY_BACKGROUND_COLOR = new JBColor(new Color(246, 241, 241), new Color(50, 50, 50));
    private static final JBColor TEXT_AREA_BACKGROUND_COLOR = new JBColor(new Color(255, 255, 255), new Color(40, 40, 40));
    private static final JBColor COPY_BUTTON_COLOR = new JBColor(new Color(0, 122, 255), new Color(0, 122, 255));
    private static final JBColor CLOSE_BUTTON_COLOR = new JBColor(new Color(255, 0, 0), new Color(255, 0, 0));
    private static final Font TEXT_AREA_FONT = new Font("Arial", Font.PLAIN, 14);
    private static final Font BUTTON_FONT = new Font("Arial", Font.BOLD, 12);
    private static String text = "";

    public static void startLogcat() {

        try {
            // NEW: Clear previously stored logs so that only logs from the new run are shown.
            logListModel.clear();
            if (popup != null && popup.isVisible()) {
                popup.cancel();
                popup = null;
            }
            logger.info("logcat listener started");
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

                long currentTimeMillis = System.currentTimeMillis();
                SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm:ss.SSS");
                Date currentDate = new Date(currentTimeMillis);
                String formattedCurrentTime = sdf.format(currentDate);
                long formattedCurrentMillis = 0;
                try {
                    formattedCurrentMillis = sdf.parse(formattedCurrentTime).getTime();
                } catch (ParseException e) {
                    logger.error("Error parsing current time: " + e.getMessage(), e);
                }

                // Extract timestamp from the beginning of the log using regex.
                Pattern datePattern = Pattern.compile("^\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\.\\d{3}");
                Matcher dateMatcher = datePattern.matcher(text);
                String timestamp = "";
                long timestampMillis = 0;
                if (dateMatcher.find()) {
                    timestamp = dateMatcher.group();
                    try {
                        Date date = sdf.parse(timestamp);
                        timestampMillis = date.getTime();
//                        logger.info("Timestamp in milliseconds: " + timestampMillis);
                    } catch (ParseException e) {
                        logger.error("Error parsing timestamp: " + e.getMessage(), e);
                    }
                }

                logger.info("Current time (millis): " + formattedCurrentMillis);
                logger.info("Log timestamp (millis): " + timestampMillis);

                if (timestampMillis >= (formattedCurrentMillis - 25000)) {
                    // Only show log if its timestamp is greater than or equal to the current time.
                    if (text.contains("CONVERSION-") || text.contains("LAUNCH-")) {
                        if (text.contains("SUCCESS") || text.contains("FAILURE")) {
                            Pattern pattern = Pattern.compile("result:\\s*\\w+");
                            Matcher matcher = pattern.matcher(text);
                            if (matcher.find()) {
                                String result = matcher.group();
                                // Append the timestamp to the result.
                                String logMessage = timestamp + " - " + result;
                                SwingUtilities.invokeLater(() -> showPopup(logMessage));
                            }
                        }
                        String formattedLog = extractKeyValueFromLog(text, timestamp);
                        if (formattedLog != null) {
                            SwingUtilities.invokeLater(() -> showPopup(formattedLog));
                        } else {
                            logger.warn("No timestamp found in log: " + text);
                        }
                    }
                }
            }
        });
        return processHandler;
    }


    // This method now updates the popup with only the logs of the current run.
    private static void showPopup(String formattedLogText) {
        // If the log is not already stored, add it.
        if (!displayedLogs.contains(formattedLogText)) {
            displayedLogs.add(formattedLogText);
            logListModel.addElement(formattedLogText);
        }

        // Create the popup if it doesn't exist.
        if (popup == null) {
            createPopup();
        }

        // Update the content of the popup with the new log list.
        updateLogPanel();
    }

    // Create the popup only once.
    private static void createPopup() {
        // Main container with BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BACKGROUND_COLOR);

        // Button panel at the top
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setBackground(BACKGROUND_COLOR);
        JButton closeButton = createCloseButton();
        buttonPanel.add(closeButton);

        // Log panel
        logPanel = new JPanel();
        logPanel.setLayout(new BoxLayout(logPanel, BoxLayout.Y_AXIS));
        logPanel.setBackground(BACKGROUND_COLOR);

        // Add button panel to the top of the main panel
        mainPanel.add(buttonPanel, BorderLayout.NORTH);

        // Add log panel to a scroll pane
        scrollPane = new JBScrollPane(logPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        scrollPane.setPreferredSize(new Dimension(400, 300));

        // Add scroll pane to the center of the main panel
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        popup = JBPopupFactory.getInstance()
                .createComponentPopupBuilder(mainPanel, null)
                .setTitle("Extracted Log")
                .setMovable(true)
                .setResizable(true)
                .setRequestFocus(true)
                .setCancelOnClickOutside(false)
                .setCancelCallback(() -> {
                    popup = null;
                    return true;
                })
                .createPopup();
        popup.showInFocusCenter();
    }


    // Update the log panel with all logs from the current run.
    private static void updateLogPanel() {
        logPanel.removeAll();

        for (int i = 0; i < logListModel.size(); i++) {
            String log = logListModel.get(i);
            JPanel entryPanel = createLogEntryPanel(log);
            logPanel.add(entryPanel);
            logPanel.add(Box.createVerticalStrut(10)); // spacing between entries
        }

        logPanel.revalidate();
        logPanel.repaint();
    }

    private static @NotNull JPanel createLogEntryPanel(String log) {
        JPanel entryPanel = new JPanel(new BorderLayout());
        entryPanel.setBackground(ENTRY_BACKGROUND_COLOR);

        JTextArea logTextArea = new JBTextArea(log);
        logTextArea.setFont(TEXT_AREA_FONT);
        logTextArea.setWrapStyleWord(true);
        logTextArea.setLineWrap(true);
        logTextArea.setEditable(false);
        logTextArea.setBackground(TEXT_AREA_BACKGROUND_COLOR);
        logTextArea.setForeground(JBColor.red);

        JButton copyButton = createCopyButton(log);

        // (Optional) You could add the close button to each entry if needed.
        // For now, we add only a copy button.
        entryPanel.add(new JBScrollPane(logTextArea), BorderLayout.CENTER);
        entryPanel.add(copyButton, BorderLayout.SOUTH);
        entryPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        entryPanel.setPreferredSize(new Dimension(180, 90));

        return entryPanel;
    }

    private static JButton createCopyButton(String log) {
        JButton copyButton = new JButton("Copy");
        copyButton.setBackground(COPY_BUTTON_COLOR);
        copyButton.setFont(BUTTON_FONT);
        copyButton.setFocusPainted(false);
        copyButton.setPreferredSize(new Dimension(100, 30));
        copyButton.addActionListener(e -> {
            copyToClipboard(log);
            // Change button color temporarily to indicate success
            Color originalColor = copyButton.getBackground();
            copyButton.setBackground(JBColor.green);
            new Timer(500, evt -> copyButton.setBackground(originalColor)).start();
        });
        return copyButton;
    }

    private static void copyToClipboard(String log) {
        StringSelection selection = new StringSelection(log);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(selection, selection);
    }

    private static JButton createCloseButton() {
        JButton closeButton = new JButton("✖");
        closeButton.setFont(new Font("Arial", Font.BOLD, 16));
        closeButton.setFocusPainted(false);
        closeButton.setBorder(BorderFactory.createEmptyBorder());
        closeButton.setPreferredSize(new Dimension(30, 30));
        closeButton.setMaximumSize(new Dimension(30, 30));
        closeButton.setMinimumSize(new Dimension(30, 30));
        closeButton.setAlignmentX(Component.LEFT_ALIGNMENT); // מבטיח שהכפתור יישאר שמאלי
        closeButton.addActionListener(e -> closePopup());
        return closeButton;
    }

    private static void closePopup() {
        if (popup != null) {
            popup.cancel();
        }
    }

    // Updated extractKeyValueFromLog now receives the timestamp as a parameter.
    private static String extractKeyValueFromLog(String logText, String timestamp) {
        try {
            int jsonStartIndex = logText.indexOf("{");
            int jsonEndIndex = logText.lastIndexOf("}");

            if (jsonStartIndex != -1 && jsonEndIndex != -1) {
                String jsonPart = logText.substring(jsonStartIndex, jsonEndIndex + 1);
                // Ensure the JSON part is complete
                if (jsonPart.chars().filter(ch -> ch == '{').count() != jsonPart.chars().filter(ch -> ch == '}').count()) {
                    logger.warn("Incomplete JSON: " + jsonPart);
                    return null;
                }
                logger.info("JSON Part: " + jsonPart);

                JsonObject jsonObject = JsonParser.parseString(jsonPart).getAsJsonObject();
                String uid = jsonObject.has("uid") ? jsonObject.get("uid").getAsString() : "UID Not Found";
                return timestamp + " - UID: " + uid;
            } else {
                return null;
            }
        } catch (JsonParseException e) {
            logger.error("Error parsing JSON from log: " + e.getMessage() + ", jsonPart=" + logText, e);
            return null;
        } catch (Exception e) {
            logger.error("Error extracting key-value from log", e);
            return null;
        }
    }
}
