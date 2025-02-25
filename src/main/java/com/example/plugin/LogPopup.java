package com.example.plugin;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextArea;
import groovyjarjarantlr4.v4.runtime.misc.NotNull;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;


public class LogPopup {
    private static  List<String> displayedLogs = new ArrayList<>();
    private static JBPopup popup;
    private static JPanel logPanel = new JPanel(); // 🔥 הגדרת לוח הלוגים כשדה גלובלי
    private static JScrollPane scrollPane;

    private static final JBColor BACKGROUND_COLOR = new JBColor(new Color(30, 30, 30), new Color(30, 30, 30));
    private static final JBColor ENTRY_BACKGROUND_COLOR = new JBColor(new Color(246, 241, 241), new Color(50, 50, 50));
    private static final JBColor TEXT_AREA_BACKGROUND_COLOR = new JBColor(new Color(255, 255, 255), new Color(40, 40, 40));
    private static final Font TEXT_AREA_FONT = new Font("Arial", Font.PLAIN, 14);
    private static String text = "";


    public static void setPopup(JBPopup newPopup) {popup = newPopup;}
    public static JBPopup getPopup() {return popup;}
    public static void setLogPanel(JPanel newLogPanel) {logPanel= newLogPanel;}
    public static JPanel getLogPanel(){return logPanel;}
    public static void setDisplayedLogs(List<String> newDisplayedLogs) {displayedLogs = newDisplayedLogs;}
    public static List<String> getDisplayedLogs(){return displayedLogs;}

    public static void showPopup(String formattedLogText) {
        if (formattedLogText.equals("new task added: LAUNCH")){
            displayedLogs.clear();
            return;
        }else if (!displayedLogs.contains(formattedLogText)) {
            displayedLogs.add(formattedLogText);
        }
        if(null == popup) {
            createPopup();
        }
        if(popup != null){
        new Thread(() -> {
            try {
                Thread.sleep(1000); // השהייה של שנייה
                SwingUtilities.invokeLater(() -> updateLogPanel()); // קריאה ל- updateLogPanel על UI Thread
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
        } else {
            updateLogPanel();
        }

    }

    private static void createPopup() {
        // Main container with BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BACKGROUND_COLOR);

        // Button panel at the top
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setBackground(BACKGROUND_COLOR);
        JButton closeButton = LogUtils.createCloseButton();
        buttonPanel.add(closeButton);

        // Log panel
        logPanel = new JPanel();
        logPanel.setLayout(new BoxLayout(logPanel, BoxLayout.Y_AXIS));
        logPanel.setBackground(BACKGROUND_COLOR);

        // Add button panel to the top of the main panel
        mainPanel.add(buttonPanel, BorderLayout.NORTH);

        // Add log panel to a scroll pane
        scrollPane = new JBScrollPane(logPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        scrollPane.setPreferredSize(new Dimension(400, 300));

        // Add scroll pane to the center of the main panel
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        popup = JBPopupFactory.getInstance()
                .createComponentPopupBuilder(mainPanel, null)
                .setTitle("Extracted Log")
                .setMovable(true)
                .setResizable(true)
                .setRequestFocus(true)
                .setCancelOnClickOutside(false)
                .setCancelCallback(() -> {
                    popup = null;
                    return true;
                })
                .createPopup();
        popup.showInFocusCenter();
    }

    public static void updateLogPanel() {
        if (popup == null) return; // 🔥 מוודא שהפופ-אפ קיים

        logPanel.removeAll(); // 🔥 מנקה את כל הלוגים הקודמים

        for (String log : displayedLogs) {
            JPanel entryPanel = createLogEntryPanel(log);
            logPanel.add(entryPanel);
            logPanel.add(Box.createVerticalStrut(10)); // מרווחים בין לוגים
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
        logTextArea.setForeground(JBColor.red);

        JButton copyButton = LogUtils.createCopyButton(log);

        // (Optional) You could add the close button to each entry if needed.
        // For now, we add only a copy button.
        entryPanel.add(new JBScrollPane(logTextArea), BorderLayout.CENTER);
        entryPanel.add(copyButton, BorderLayout.SOUTH);
        entryPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        entryPanel.setPreferredSize(new Dimension(180, 90));

        return entryPanel;
    }
}
