package com.example.plugin;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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

            if (jsonStartIndex != -1 && jsonEndIndex != -1) {
                String jsonPart = logText.substring(jsonStartIndex, jsonEndIndex + 1);
                JsonObject jsonObject = JsonParser.parseString(jsonPart).getAsJsonObject();
                String uid = jsonObject.has("uid") ? jsonObject.get("uid").getAsString() : "UID Not Found";
                return "UID: " + uid;
            } else {
                return null;
            }
        } catch (Exception e) {
            logger.error("Error extracting key-value from log", e);
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


    private static void closePopup() {
        if (LogPopup.getPopup() != null) {
            LogPopup.getPopup().cancel();
            LogPopup.setPopup(null);
        }
    }
}