package com.example.plugin;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.intellij.ui.JBColor;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Clipboard;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogUtils {

    // method to extract needed info from logs
    public static String extractMessageFromJson(String type, String logText) {
        try {
            int jsonStartIndex = logText.indexOf("{");
            int jsonEndIndex = logText.lastIndexOf("}");

            // Check if indices are valid
            if (jsonStartIndex == -1 || jsonEndIndex == -1 || jsonEndIndex < jsonStartIndex) {
                System.err.println("Error: JSON not found in log text.");
                return null;
            }

            // Check if indices are within string bounds
            if (jsonEndIndex + 1 > logText.length()) {
                System.err.println("Error: Invalid substring range.");
                return null;
            }

            String jsonPart = logText.substring(jsonStartIndex, jsonEndIndex + 1).trim();
            JsonObject jsonObject = JsonParser.parseString(jsonPart).getAsJsonObject();

            if(type.equals("CONVERSION") || type.equals("LAUNCH")){
                return  jsonObject.has("uid") ? "UID: " + jsonObject.get("uid").getAsString() : "UID Not Found";
            } else if (type.equals("INAPP")){
                String eventName = jsonObject.has("eventName") ? jsonObject.get("eventName").getAsString() : "Event Name Not Found";
                String eventValue = jsonObject.has("eventValue") ? jsonObject.get("eventValue").getAsString() : "Event Value Not Found";
                return "\n"+"{"+"\n"+"Event Name: " + eventName + ","+"\n"+" Event Value: " + eventValue+"\n"+"}";
            }
            return null;

        } catch (JsonSyntaxException e) {
            System.err.println("JSON parsing error: " + e.getMessage());
            return null;
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            return null;
        }
    }

    // method to extract event info from logs
    public static String extractEventFromLog(String logText) {
        try {
            StringBuilder result = new StringBuilder();

            // Extract event name and value using regex
            Pattern eventPattern = Pattern.compile("\"event\":\\s*\"([^\"]+)\"");
            Pattern valuePattern = Pattern.compile("\"eventvalue\":\\s*\\{([^}]+)}");

            Matcher eventMatcher = eventPattern.matcher(logText);
            if (eventMatcher.find()) {
                String eventName = eventMatcher.group(1);
                result.append("Event: ").append(eventName);
            }

            Matcher valueMatcher = valuePattern.matcher(logText);
            if (valueMatcher.find()) {
                String eventValue = valueMatcher.group(1);
                result.append(" | Value: ").append(eventValue);
            }

            // Check for app_id in log
            if (logText.contains("androidevent?app_id=")) {
                Pattern appIdPattern = Pattern.compile("app_id=([^\\s&]+)");
                Matcher appIdMatcher = appIdPattern.matcher(logText);
                if (appIdMatcher.find()) {
                    String appId = appIdMatcher.group(1);
                    result.append(" | App ID: ").append(appId);
                }
            }

            return !result.isEmpty() ? result.toString() : null;
        } catch (Exception e) {
            System.err.println("Error extracting event: " + e.getMessage());
            return null;
        }
    }

    public static JButton createCopyButton(String log) {
        JButton copyButton = new JButton("Copy");

        copyButton.setBackground(null);
        copyButton.setOpaque(false);
        copyButton.setFont(new Font("Arial", Font.BOLD, 12));
        copyButton.setFocusPainted(false);
        copyButton.setBorder(BorderFactory.createEmptyBorder(8,10,8,10));

        copyButton.setPreferredSize(new Dimension(50, 40));

        copyButton.addActionListener(e -> {
            String toCopy = log;
            int uidIndex = log.indexOf("UID:");
            if (uidIndex != -1) {
                toCopy = log.substring(uidIndex + "UID:".length()).trim();
            } else if (log.contains("Event:")) {
                // Copy event information
                toCopy = log.substring(log.indexOf("Event:"));
            }
            copyToClipboard(toCopy);

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

}