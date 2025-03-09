// LogUtils.java - Modified version
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

    private static final JBColor COPY_BUTTON_COLOR = new JBColor(new Color(0, 122, 255), new Color(0, 122, 255));
    private static final Font BUTTON_FONT = new Font("Arial", Font.BOLD, 12);


    public static String extractKeyValueFromLog(String logText) {

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


            System.out.println("Extracted JSON: " + jsonPart); // Print JSON for debugging

            // Parse JSON
            JsonObject jsonObject = JsonParser.parseString(jsonPart).getAsJsonObject();
            String uid = jsonObject.has("uid") ? jsonObject.get("uid").getAsString() : "UID Not Found";

            return "UID: " + uid;
        } catch (JsonSyntaxException e) {
            System.err.println("JSON parsing error: " + e.getMessage());
            return null;
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            return null;
        }
    }

    public static JButton createCopyButton(String log) {
        JButton copyButton = new JButton("Copy");
        copyButton.setBackground(COPY_BUTTON_COLOR);
        copyButton.setFont(BUTTON_FONT);
        copyButton.setFocusPainted(false);
        copyButton.setPreferredSize(new Dimension(100, 30));
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

    public static void clearLogs() {
        LogPopup.getDisplayedLogs().clear();
        LogPopup.getLogPanel().removeAll();
        LogPopup.getLogPanel().revalidate();
        LogPopup.getLogPanel().repaint();
    }
}