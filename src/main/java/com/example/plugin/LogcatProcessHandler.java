package com.example.plugin;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

public class LogcatProcessHandler {
    private static final Logger logger = Logger.getInstance(LogcatProcessHandler.class);
    private static final Set<String> displayedLogs = new HashSet<>();
    private static final DefaultListModel<String> logListModel = new DefaultListModel<>();

    // משתנה סטטי לפופאפ
    private static JBPopup popup;

    public static void startLogcat() {
        try {
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
                if (text.contains("CONVERSION-") || text.contains("LAUNCH-")) {
                    String formattedLog = extractKeyValueFromLog(text);
                    if (formattedLog != null) {
                        showPopup(formattedLog);
                    }
                }
            }
        });
        return processHandler;
    }

    private static void showPopup(String formattedLogText) {
        if (!displayedLogs.contains(formattedLogText)) {
            displayedLogs.add(formattedLogText);
            logListModel.addElement(formattedLogText);
        }

        JPanel logPanel = new JPanel();
        logPanel.setLayout(new BoxLayout(logPanel, BoxLayout.Y_AXIS));
        logPanel.setBackground(new JBColor(new Color(30, 30, 30), new Color(30, 30, 30)));

        for (int i = 0; i < logListModel.size(); i++) {
            String log = logListModel.get(i);
            JPanel entryPanel = createLogEntryPanel(log);
            logPanel.add(entryPanel);
            logPanel.add(Box.createVerticalStrut(10)); // Spacing between logs
        }

        JScrollPane scrollPane = new JBScrollPane(logPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        popup = JBPopupFactory.getInstance()
                .createComponentPopupBuilder(scrollPane, null)
                .setTitle("Extracted Log")
                .setMovable(true)
                .setResizable(true)
                .setRequestFocus(true)
                .setCancelOnClickOutside(true)
                .createPopup();

        SwingUtilities.invokeLater(popup::showInFocusCenter);
        logger.info("Popup displayed!");
    }

    private static @NotNull JPanel createLogEntryPanel(String log) {
        JPanel entryPanel = new JPanel(new BorderLayout());
        entryPanel.setBackground(new JBColor(new Color(50, 50, 50), new Color(50, 50, 50)));

        JTextArea logTextArea = new JBTextArea(log);
        logTextArea.setFont(new Font("Arial", Font.PLAIN, 14));
        logTextArea.setWrapStyleWord(true);
        logTextArea.setLineWrap(true);
        logTextArea.setEditable(false);
        logTextArea.setBackground(new JBColor(new Color(36, 36, 36), new Color(40, 40, 40)));
        logTextArea.setForeground(JBColor.white);

        JButton copyButton = createCopyButton(log);
        JButton closeButton = createCloseButton();

        JPanel closeButtonPanel = new JPanel(new BorderLayout());
        closeButtonPanel.setBackground(new JBColor(new Color(50, 50, 50), new Color(50, 50, 50)));
        closeButtonPanel.add(closeButton, BorderLayout.EAST);

        entryPanel.add(new JBScrollPane(logTextArea), BorderLayout.CENTER);
        entryPanel.add(copyButton, BorderLayout.SOUTH);
        entryPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        entryPanel.setPreferredSize(new Dimension(500, 100));

        return entryPanel;
    }

    private static JButton createCopyButton(String log) {
        JButton copyButton = new JButton("Copy");
        copyButton.setBackground(new JBColor(new Color(0, 122, 255), new Color(0, 122, 255)));
        copyButton.setForeground(JBColor.white);
        copyButton.setFont(new Font("Arial", Font.BOLD, 12));
        copyButton.setFocusPainted(false);
        copyButton.setBorder(BorderFactory.createLineBorder(new JBColor(new Color(0, 122, 255), new Color(0, 122, 255))));
        copyButton.setPreferredSize(new Dimension(100, 30));
        copyButton.addActionListener(e -> copyToClipboard(log));
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
        closeButton.setForeground(JBColor.white);
        closeButton.setBackground(new JBColor(new Color(255, 0, 0), new Color(255, 0, 0)));
        closeButton.setFocusPainted(false);
        closeButton.setBorder(BorderFactory.createEmptyBorder());
        closeButton.setPreferredSize(new Dimension(30, 30));
        closeButton.addActionListener(e -> closePopup());
        return closeButton;
    }

    private static void closePopup() {
        if (popup != null) {
            popup.cancel();
        }
    }

    private static String extractKeyValueFromLog(String logText) {
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
}
