package com.example.plugin;

import com.intellij.ui.Gray;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextArea;
import com.intellij.util.ui.JBUI;
import groovyjarjarantlr4.v4.runtime.misc.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

public class LogPopup {
    private static List<String> displayedLogs = new ArrayList<>();

    // Replaced JBPopup with JDialog
    private static JDialog popup;

    private static JPanel logPanel = new JPanel();

    private static final JBColor BACKGROUND_COLOR = new JBColor(Gray._30, Gray._30);
    private static final JBColor ENTRY_BACKGROUND_COLOR = new JBColor(new Color(246, 241, 241), Gray._50);
    private static final JBColor TEXT_AREA_BACKGROUND_COLOR = new JBColor(Gray._255, Gray._40);
    private static final Font TEXT_AREA_FONT = new Font("Arial", Font.PLAIN, 14);
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

    static void createPopup() {
        // Main container with BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BACKGROUND_COLOR);

        // Button panel at the top
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setBackground(BACKGROUND_COLOR);

        // Create buttons
        JToggleButton showAllButton = new JToggleButton("All");
        JToggleButton showLaunchButton = new JToggleButton("LAUNCH");
        JToggleButton showConversionButton = new JToggleButton("CONVERSION");
        JToggleButton showEventButton = new JToggleButton("EVENT");
        JButton changeDeviceButton = new JButton("Change Device");

        // Add action to change device button
        changeDeviceButton.addActionListener(e -> {
            // Reset device and restart logcat
            LogcatProcessHandler.resetSelectedDevice();
            LogUtils.clearLogs();
            LogcatProcessHandler.startLogcat();
        });

        // Set selected by default
        showAllButton.setSelected(true);

        // Button group for filter buttons
        ButtonGroup filterGroup = new ButtonGroup();
        filterGroup.add(showAllButton);
        filterGroup.add(showLaunchButton);
        filterGroup.add(showConversionButton);
        filterGroup.add(showEventButton);

        // Add filter action listeners
        showAllButton.addActionListener(e -> filterLogs(null));
        showLaunchButton.addActionListener(e -> filterLogs("LAUNCH"));
        showConversionButton.addActionListener(e -> filterLogs("CONVERSION"));
        showEventButton.addActionListener(e -> filterLogs("EVENT"));


        // Add buttons to panel in correct order
        buttonPanel.add(showAllButton);
        buttonPanel.add(showLaunchButton);
        buttonPanel.add(showConversionButton);
        buttonPanel.add(showEventButton);
        buttonPanel.add(Box.createHorizontalStrut(20)); // Add spacing
        buttonPanel.add(changeDeviceButton);

        // Add refresh button
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> {
            // Keep only CONVERSION logs
            List<String> conversionLogs = displayedLogs.stream()
                    .filter(log -> log.contains("CONVERSION"))
                    .toList();

            displayedLogs.clear();
            displayedLogs.addAll(conversionLogs);

            // Update panel
            updateLogPanel();

            // Restart logcat
            LogcatProcessHandler.startLogcat();
        });


        // Add buttons to panel in correct order
        buttonPanel.add(showAllButton);
        buttonPanel.add(showLaunchButton);
        buttonPanel.add(showConversionButton);
        buttonPanel.add(showEventButton);
        buttonPanel.add(refreshButton);

        // Log panel
        logPanel = new JPanel();
        logPanel.setLayout(new BoxLayout(logPanel, BoxLayout.Y_AXIS));
        logPanel.setBackground(BACKGROUND_COLOR);

        // Add button panel to the top of the main panel
        mainPanel.add(buttonPanel, BorderLayout.NORTH);

        // Add log panel to a scroll pane
        JScrollPane scrollPane = new JBScrollPane(logPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        scrollPane.setPreferredSize(new Dimension(600, 400));

        // Add scroll pane to the center of the main panel
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Create JDialog
        popup = new JDialog();
        popup.setTitle("Logcat Monitor");
        popup.setModal(false);
        popup.setResizable(true);
        popup.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        popup.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                popup = null;
            }
        });

        popup.getContentPane().add(mainPanel);
        popup.pack();
        popup.setLocationRelativeTo(null);
        popup.setVisible(true);
    }

    public static void showPopup(String log) {
        if (popup == null) {
            createPopup();
        }

        // Add log only if it doesn't exist yet
        if (!displayedLogs.contains(log)) {
            displayedLogs.add(log);
            updateLogPanel();

            // Scroll to bottom
            SwingUtilities.invokeLater(() -> {
                Component[] components = logPanel.getComponents();
                if (components.length > 0) {
                    Rectangle bounds = components[components.length - 1].getBounds();
                    logPanel.scrollRectToVisible(bounds);
                }
            });
        }
    }

    public static void updateLogPanel() {
        if (popup == null) return;

        logPanel.removeAll();

        // Get all unique LAUNCH and CONVERSION logs
        List<String> launchLogs = displayedLogs.stream()
                .filter(log -> log.contains("/ LAUNCH"))
                .toList();

        List<String> conversionLogs = displayedLogs.stream()
                .filter(log -> log.contains("/ CONVERSION"))
                .toList();

        List<String> eventLogs = displayedLogs.stream()
                .filter(log -> log.contains("/ EVENT"))
                .toList();

        // Create final list: all LAUNCH and CONVERSION logs, but only most recent EVENT
        List<String> logsToShow = new ArrayList<>();
        logsToShow.addAll(launchLogs);  // Add all LAUNCH logs
        logsToShow.addAll(conversionLogs);  // Add all CONVERSION logs
        if (!eventLogs.isEmpty()) {
            logsToShow.add(eventLogs.get(eventLogs.size() - 1));  // Add only most recent EVENT
        }

        // Add panels for all logs
        for (String log : logsToShow) {
            JPanel entryPanel = createLogEntryPanel(log);
            logPanel.add(entryPanel);
            logPanel.add(Box.createVerticalStrut(10));
        }

        logPanel.revalidate();
        logPanel.repaint();
    }

    private static void filterLogs(String filterType) {
        if (popup == null) return;
        logPanel.removeAll();

        if (filterType == null) {
            // Show all types
            updateLogPanel();
        } else {
            List<String> filteredLogs = displayedLogs.stream()
                    .filter(log -> log.contains("/ " + filterType))
                    .toList();

            if (!filteredLogs.isEmpty()) {
                if (filterType.equals("EVENT")) {
                    // For EVENT, show only most recent
                    String mostRecent = filteredLogs.get(filteredLogs.size() - 1);
                    JPanel entryPanel = createLogEntryPanel(mostRecent);
                    logPanel.add(entryPanel);
                } else {
                    // For LAUNCH and CONVERSION, show all unique logs
                    for (String log : filteredLogs) {
                        JPanel entryPanel = createLogEntryPanel(log);
                        logPanel.add(entryPanel);
                        logPanel.add(Box.createVerticalStrut(10));
                    }
                }
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

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonsPanel.setBackground(ENTRY_BACKGROUND_COLOR);

        if (log.contains("/ EVENT:")) {
            logTextArea.setForeground(JBColor.GREEN);

            // Only add JSON copy button for events
            JButton jsonCopyButton = new JButton("Copy JSON");
            jsonCopyButton.setBackground(new JBColor(new Color(0, 150, 50), new Color(0, 150, 50)));
            jsonCopyButton.setFont(new Font("Arial", Font.BOLD, 12));
            jsonCopyButton.setFocusPainted(false);
            jsonCopyButton.setPreferredSize(new Dimension(100, 30));

            jsonCopyButton.addActionListener(e -> {
                String jsonData = extractJsonFromEventLog(log);
                if (jsonData != null) {
                    StringSelection selection = new StringSelection(jsonData);
                    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                    clipboard.setContents(selection, selection);

                    // Visual feedback
                    jsonCopyButton.setBackground(JBColor.green);
                    new Timer(500, evt ->
                            jsonCopyButton.setBackground(new JBColor(new Color(0, 150, 50), new Color(0, 150, 50)))
                    ).start();
                }
            });

            buttonsPanel.add(jsonCopyButton);
        } else if (log.contains("/ LAUNCH")) {
            logTextArea.setForeground(JBColor.BLUE);
            if (log.contains("UID")) {
                buttonsPanel.add(LogUtils.createCopyButton(log));
            }
        } else if (log.contains("CONVERSION")) {
            logTextArea.setForeground(JBColor.yellow);
        } else {
            logTextArea.setForeground(JBColor.GRAY);
        }

        JScrollPane textScrollPane = new JBScrollPane(logTextArea);
        textScrollPane.setBorder(BorderFactory.createEmptyBorder());

        entryPanel.add(textScrollPane, BorderLayout.CENTER);
        entryPanel.add(buttonsPanel, BorderLayout.SOUTH);
        entryPanel.setBorder(JBUI.Borders.empty(10));
        entryPanel.setPreferredSize(new Dimension(550, 100));

        return entryPanel;
    }

    private static String extractJsonFromEventLog(String log) {
        try {
            int startIndex = log.indexOf("preparing data:") + "preparing data:".length();
            String jsonPart = log.substring(startIndex).trim();

            // Clean up the JSON string if needed
            return jsonPart.replaceAll("\\\\", "");
        } catch (Exception e) {
            System.err.println("Error extracting JSON: " + e.getMessage());
            return null;
        }
    }
}