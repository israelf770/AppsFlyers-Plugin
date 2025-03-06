package com.example.plugin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.intellij.ui.JBColor;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Clipboard;
import java.util.HashMap;
import java.util.Map;

public class LogUtils {
    private static final JBColor COPY_BUTTON_COLOR = new JBColor(new Color(0, 122, 255), new Color(0, 122, 255));
    private static final Font BUTTON_FONT = new Font("Arial", Font.BOLD, 12);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    // Important keys to extract from the event
    private static final String[] IMPORTANT_KEYS = {
            "eventName",
            "uid",
            "country",
            "advertiserId",
            "app_version_name"
    };

    // Important keys to extract from eventValue
    private static final String[] IMPORTANT_EVENT_VALUE_KEYS = {
            "af_score",
            "af_content_type"
    };

    public static String extractEventFromLog(String logText) {
        try {
            // Check if it's an event log
            if (!logText.contains("INAPP-") || !logText.contains("eventName")) {
                return null;
            }

            // Find the JSON part
            int jsonStartIndex = logText.indexOf("{");
            int jsonEndIndex = logText.lastIndexOf("}");

            if (jsonStartIndex == -1 || jsonEndIndex == -1 || jsonEndIndex < jsonStartIndex) {
                System.err.println("Error: JSON not found in log text.");
                return null;
            }

            String jsonPart = logText.substring(jsonStartIndex, jsonEndIndex + 1).trim();
            JsonObject jsonObject = JsonParser.parseString(jsonPart).getAsJsonObject();

            // Create a map to hold important event details
            Map<String, Object> importantDetails = new HashMap<>();

            // Extract important keys from main event
            for (String key : IMPORTANT_KEYS) {
                if (jsonObject.has(key)) {
                    importantDetails.put(key, jsonObject.get(key).getAsString());
                }
            }

            // Extract important event value details
            String eventValueRaw = jsonObject.has("eventValue")
                    ? jsonObject.get("eventValue").getAsString()
                    : "{}";

            JsonObject eventValue = JsonParser.parseString(eventValueRaw).getAsJsonObject();
            Map<String, Object> importantEventValueDetails = new HashMap<>();

            for (String key : IMPORTANT_EVENT_VALUE_KEYS) {
                if (eventValue.has(key)) {
                    importantEventValueDetails.put(key, eventValue.get(key).getAsString());
                }
            }

            // Add event value details if not empty
            if (!importantEventValueDetails.isEmpty()) {
                importantDetails.put("eventDetails", importantEventValueDetails);
            }

            // Prepare display text
            StringBuilder displayText = new StringBuilder();
            for (Map.Entry<String, Object> entry : importantDetails.entrySet()) {
                displayText.append(entry.getKey())
                        .append(": ")
                        .append(entry.getValue())
                        .append("\n");
            }

            // Store full JSON for copy
            return GSON.toJson(importantDetails);
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            return null;
        }
    }

    public static JButton createCopyButton(String log) {
        // Check if log is not empty or null
        if (log == null || log.trim().isEmpty()) {
            return null;
        }

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

    public static void clearLogs() {
        LogPopup.getDisplayedLogs().clear();
        LogPopup.getLogPanel().removeAll();
        LogPopup.getLogPanel().revalidate();
        LogPopup.getLogPanel().repaint();
    }
}