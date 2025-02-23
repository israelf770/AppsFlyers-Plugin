package com.example.plugin;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.JBColor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class LogPopup {
    private static final List<String> displayedLogs = new ArrayList<>();
    private static JBPopup popup;

    public static void setPopup(JBPopup newPopup) {
        popup = newPopup;
    }
    public static JBPopup getPopup() {
        return popup;
    }

    public static void showPopup(String formattedLogText) {
        if (!displayedLogs.contains(formattedLogText)) {
            displayedLogs.add(formattedLogText);
        }

        JPanel logPanel = createLogPanel();

        // הגדרת הפופ-אפ
        JScrollPane scrollPane = new JScrollPane(logPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        setPopup(JBPopupFactory.getInstance()
                .createComponentPopupBuilder(scrollPane, null)
                .setTitle("Extracted Log")
                .setMovable(true)
                .setResizable(true)
                .setRequestFocus(true)
                .setCancelOnClickOutside(true)
                .createPopup());

        SwingUtilities.invokeLater(popup::showInFocusCenter);
    }

    private static JPanel createLogPanel() {
        JPanel logPanel = new JPanel();
        logPanel.setLayout(new BoxLayout(logPanel, BoxLayout.Y_AXIS));
        logPanel.setBackground(new JBColor(new Color(30, 30, 30), new Color(30, 30, 30)));

        for (String log : displayedLogs) {
            JPanel entryPanel = createLogEntryPanel(log);
            logPanel.add(entryPanel);
            logPanel.add(Box.createVerticalStrut(10)); // Spacing between logs
        }

        return logPanel;
    }

    private static JPanel createLogEntryPanel(String log) {
        JPanel entryPanel = new JPanel(new BorderLayout());
        entryPanel.setBackground(new JBColor(new Color(50, 50, 50), new Color(50, 50, 50)));

        JTextArea logTextArea = new JTextArea(log);
        logTextArea.setFont(new Font("Arial", Font.PLAIN, 14));
        logTextArea.setWrapStyleWord(true);
        logTextArea.setLineWrap(true);
        logTextArea.setEditable(false);
        logTextArea.setBackground(new JBColor(new Color(36, 36, 36), new Color(40, 40, 40)));
        logTextArea.setForeground(JBColor.white);

        JButton copyButton = LogUtils.createCopyButton(log);
        JButton closeButton = LogUtils.createCloseButton();

        JPanel closeButtonPanel = new JPanel(new BorderLayout());
        closeButtonPanel.setBackground(new JBColor(new Color(50, 50, 50), new Color(50, 50, 50)));
        closeButtonPanel.add(closeButton, BorderLayout.EAST);

        entryPanel.add(new JScrollPane(logTextArea), BorderLayout.CENTER);
        entryPanel.add(copyButton, BorderLayout.SOUTH);
        entryPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        entryPanel.setPreferredSize(new Dimension(500, 100));

        return entryPanel;
    }
}
