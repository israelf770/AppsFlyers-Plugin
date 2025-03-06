package com.example.plugin;

import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextArea;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import static com.example.plugin.LogUtils.createTextPanel;

public class LogPopup {
    // Static variables with proper access modifiers
    private static List<String> displayedLogs = new ArrayList<>();
    private static JDialog popup;
    private static JPanel logPanel = new JPanel();

    // Color and Font Definitions
    private static final JBColor BACKGROUND_COLOR = new JBColor(new Color(30, 30, 30), new Color(30, 30, 30));
    private static final JBColor ENTRY_BACKGROUND_COLOR = new JBColor(new Color(246, 241, 241), new Color(50, 50, 50));
    private static final JBColor TEXT_AREA_BACKGROUND_COLOR = new JBColor(new Color(255, 255, 255), new Color(40, 40, 40));
    private static final Font TEXT_AREA_FONT = new Font("Arial", Font.PLAIN, 14);

    // Getter and Setter for displayedLogs
    public static List<String> getDisplayedLogs() {
        return displayedLogs;
    }

    public static void setDisplayedLogs(List<String> logs) {
        displayedLogs = logs;
    }

    // Getter and Setter for popup
    public static JDialog getPopup() {
        return popup;
    }

    public static void setPopup(JDialog dialog) {
        popup = dialog;
    }

    // Getter and Setter for logPanel
    public static JPanel getLogPanel() {
        return logPanel;
    }

    public static void setLogPanel(JPanel panel) {
        logPanel = panel;
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

        if (popup != null) {
            new Thread(() -> {
                try {
                    Thread.sleep(1000);
                    SwingUtilities.invokeLater(() -> updateLogPanel());
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

        // Log panel
        logPanel = new JPanel();
        logPanel.setLayout(new BoxLayout(logPanel, BoxLayout.Y_AXIS));
        logPanel.setBackground(BACKGROUND_COLOR);

        // Add button panel to the top of the main panel
        mainPanel.add(buttonPanel, BorderLayout.NORTH);

        // Add log panel to a scroll pane
        JScrollPane scrollPane = new JBScrollPane(logPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        scrollPane.setPreferredSize(new Dimension(400, 300));

        // Add scroll pane to the center of the main panel
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Create JDialog
        popup = new JDialog();
        popup.setTitle("Extracted Log");
        popup.setModal(false);
        popup.setResizable(true);
        popup.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        popup.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
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
        createTextPanel("Loaded");
    }

    public static void updateLogPanel() {
        if (popup == null) return;

        logPanel.removeAll();

        for (String log : displayedLogs) {
            JPanel entryPanel = createLogEntryPanel(log);
            logPanel.add(entryPanel);
            logPanel.add(Box.createVerticalStrut(10));
        }

        logPanel.revalidate();
        logPanel.repaint();
    }

    public static JPanel createLogEntryPanel(String log) {
        JPanel entryPanel = new JPanel(new BorderLayout());
        entryPanel.setBackground(ENTRY_BACKGROUND_COLOR);

        JTextArea logTextArea = new JBTextArea(log);
        logTextArea.setFont(TEXT_AREA_FONT);
        logTextArea.setWrapStyleWord(true);
        logTextArea.setLineWrap(true);
        logTextArea.setEditable(false);
        logTextArea.setBackground(TEXT_AREA_BACKGROUND_COLOR);
        logTextArea.setForeground(JBColor.red);

        // Create a panel for buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setBackground(entryPanel.getBackground());

        // Try to create a copy button
        if(!log.equals("Loaded")){
            JButton copyButton = LogUtils.createCopyButton(log);
            if (copyButton != null) {
                buttonPanel.add(copyButton);
            }
        }

        // Add text area & button panel
        entryPanel.add(new JBScrollPane(logTextArea), BorderLayout.CENTER);
        entryPanel.add(buttonPanel, BorderLayout.SOUTH);
        entryPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        entryPanel.setPreferredSize(new Dimension(180, 120));

        return entryPanel;
    }
}