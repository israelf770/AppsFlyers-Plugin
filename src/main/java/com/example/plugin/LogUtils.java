package com.example.plugin;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.intellij.openapi.diagnostic.Logger;

import javax.swing.*;
import java.awt.datatransfer.StringSelection;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;

public class LogUtils {
    private static final Logger logger = Logger.getInstance(LogUtils.class);

    public static String extractKeyValueFromLog(String logText) {
        try {
            int jsonStartIndex = logText.indexOf("{");
            int jsonEndIndex = logText.lastIndexOf("}");

            // בדיקה שהאינדקסים תקפים
            if (jsonStartIndex == -1 || jsonEndIndex == -1 || jsonEndIndex < jsonStartIndex) {
                System.err.println("Error: JSON not found in log text.");
                return null;
            }

            // בדיקה שהאינדקסים לא חורגים מאורך המחרוזת
            if (jsonEndIndex + 1 > logText.length()) {
                System.err.println("Error: Invalid substring range.");
                return null;
            }

            String jsonPart = logText.substring(jsonStartIndex, jsonEndIndex + 1).trim();

            System.out.println("Extracted JSON: " + jsonPart); // הדפסת JSON לבדיקה

            // פרסינג ל-JSON
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
        copyButton.addActionListener(e -> copyToClipboard(log));
        return copyButton;
    }

    public static void copyToClipboard(String log) {
        StringSelection selection = new StringSelection(log);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(selection, selection);
    }

    public static JButton createCloseButton() {
        JButton closeButton = new JButton("✖");
        closeButton.addActionListener(e -> closePopup());
        return closeButton;
    }


    public static void closePopup() {
        if (LogPopup.getPopup() != null && LogPopup.getPopup().isVisible()) {
            LogPopup.getPopup().cancel();
            LogPopup.setPopup(null);
            clearLogs(); // ניקוי רשימת הלוגים
        }
    }

    public static void clearLogs() {
        LogPopup.getDisplayedLogs().clear();
        LogPopup.getLogPanel().removeAll();
        LogPopup.getLogPanel().revalidate();
        LogPopup.getLogPanel().repaint();
    }
}