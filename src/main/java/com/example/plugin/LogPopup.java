package com.example.plugin;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.ui.popup.JBPopupFactory; // Note: no longer used, but you can remove if you like
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextArea;
import groovyjarjarantlr4.v4.runtime.misc.NotNull;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

public class LogPopup {
    private static List<String> displayedLogs = new ArrayList<>();

    // Replaced JBPopup with JDialog
    private static JDialog popup;

    private static JPanel logPanel = new JPanel();

    private static final JBColor BACKGROUND_COLOR = new JBColor(new Color(30, 30, 30), new Color(30, 30, 30));
    private static final JBColor ENTRY_BACKGROUND_COLOR = new JBColor(new Color(246, 241, 241), new Color(50, 50, 50));
    private static final JBColor TEXT_AREA_BACKGROUND_COLOR = new JBColor(new Color(255, 255, 255), new Color(40, 40, 40));
    private static final Font TEXT_AREA_FONT = new Font("Arial", Font.PLAIN, 14);
    private static String text = "";

    // Getters/Setters
    public static void setPopup(JDialog newPopup) {
        popup = newPopup;
    }

    public static JDialog getPopup() {
        return popup;
    }

    public static void setLogPanel(JPanel newLogPanel) {
        logPanel = newLogPanel;
    }

    public static JPanel getLogPanel() {
        return logPanel;
    }

    public static void setDisplayedLogs(List<String> newDisplayedLogs) {
        displayedLogs = newDisplayedLogs;
    }

    public static List<String> getDisplayedLogs() {
        return displayedLogs;
    }

    public static void showPopup(String formattedLogText) {
        if (formattedLogText.equals("new task added: LAUNCH")) {
            displayedLogs.clear();
            return;
        } else if (!displayedLogs.contains(formattedLogText)) {
            displayedLogs.add(formattedLogText);
        }

        // If we haven't created a JDialog yet, do it now
        if (popup == null) {
            createPopup();
        }
        if(popup != null){
            new Thread(() -> {
                try {
                    Thread.sleep(400); // Delay of 400ms
                    SwingUtilities.invokeLater(() -> updateLogPanel()); // Call updateLogPanel on UI Thread
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    private static void createPopup() {

        // Main container with BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BACKGROUND_COLOR);

        // Button panel at the top
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setBackground(BACKGROUND_COLOR);

        // Add filter buttons
        JToggleButton showAllButton = new JToggleButton("All");
        JToggleButton showConversionButton = new JToggleButton("CONVERSION");
        JToggleButton showLaunchButton = new JToggleButton("LAUNCH");
        JToggleButton showEventButton = new JToggleButton("EVENT");

        // Set selected by default
        showAllButton.setSelected(true);

        // Button group to make them mutually exclusive
        ButtonGroup filterGroup = new ButtonGroup();
        filterGroup.add(showAllButton);
        filterGroup.add(showConversionButton);
        filterGroup.add(showLaunchButton);
        filterGroup.add(showEventButton);

        // Add filter action listeners
        showAllButton.addActionListener(e -> filterLogs(null));
        showConversionButton.addActionListener(e -> filterLogs("CONVERSION"));
        showLaunchButton.addActionListener(e -> filterLogs("LAUNCH"));
        showEventButton.addActionListener(e -> filterLogs("INAPP"));

        // Add clear button
        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(e -> LogUtils.clearLogs());

        // Add buttons to panel
        buttonPanel.add(showAllButton);
        buttonPanel.add(showConversionButton);
        buttonPanel.add(showLaunchButton);
        buttonPanel.add(showEventButton);
        buttonPanel.add(clearButton);

        // Log panel
        logPanel = new JPanel();
        logPanel.setLayout(new BoxLayout(logPanel, BoxLayout.Y_AXIS));
        logPanel.setBackground(BACKGROUND_COLOR);

        // Add button panel to the top of the main panel
        mainPanel.add(buttonPanel, BorderLayout.NORTH);

        // Add log panel to a scroll pane
        JScrollPane scrollPane = new JBScrollPane(logPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        scrollPane.setPreferredSize(new Dimension(600, 400)); // Larger size for better visibility

        // Add scroll pane to the center of the main panel
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // === Create JDialog in place of JBPopup ===
        popup = new JDialog();
        popup.setTitle("Logcat Monitor");
        popup.setModal(false);        // Non-blocking
        popup.setResizable(true);     // Make it resizable
        popup.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        popup.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                // When window is closed (after DISPOSE), reset the variable
                popup = null;
            }
        });

        // Add our mainPanel to the JDialog's content pane
        popup.getContentPane().add(mainPanel);

        // Pack and center
        popup.pack();
        popup.setLocationRelativeTo(null);

        // Make sure to show it
        popup.setVisible(true);
    }

    public static void updateLogPanel() {
        if (popup == null) return;

        logPanel.removeAll();

        for (String log : displayedLogs) {
            JPanel entryPanel = createLogEntryPanel(log);
            logPanel.add(entryPanel);
            logPanel.add(Box.createVerticalStrut(10)); // Space between logs
        }

        logPanel.revalidate();
        logPanel.repaint();
    }

    // New method to filter logs by type
    private static void filterLogs(String filterType) {
        if (popup == null) return;

        logPanel.removeAll();

        for (String log : displayedLogs) {
            // If no filter or log contains the filter type, add it
            if (filterType == null || log.contains("/ " + filterType)) {
                JPanel entryPanel = createLogEntryPanel(log);
                logPanel.add(entryPanel);
                logPanel.add(Box.createVerticalStrut(10));
            }
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

        // Color-code by log type
        if (log.contains("/ INAPP:" ) || log.contains("Event Name")) {
            logTextArea.setForeground(JBColor.GREEN);
        } else if (log.contains("CONVERSION")) {
            logTextArea.setForeground(JBColor.RED);
        } else if (log.contains("LAUNCH")) {
            logTextArea.setForeground(JBColor.BLUE);
        } else {
            logTextArea.setForeground(JBColor.GRAY);
        }

        // Add copy button for logs with copyable content
        if (log.contains("UID") || log.contains("Event:") || log.contains("app_id=")) {
            JButton copyButton = LogUtils.createCopyButton(log);
            entryPanel.add(copyButton, BorderLayout.SOUTH);
        }

        // Add text area to a scroll pane for long content
        JScrollPane textScrollPane = new JBScrollPane(logTextArea);
        textScrollPane.setBorder(BorderFactory.createEmptyBorder());

        // Add text area & copy button
        entryPanel.add(textScrollPane, BorderLayout.CENTER);
        entryPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        entryPanel.setPreferredSize(new Dimension(550, 100)); // Taller for better visibility

        return entryPanel;
    }
}