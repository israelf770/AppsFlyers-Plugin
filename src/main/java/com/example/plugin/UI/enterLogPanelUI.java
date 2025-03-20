package com.example.plugin.UI;

import com.example.plugin.LogcatNavigator;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.Gray;
import com.intellij.ui.JBColor;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;
import javax.swing.*;
import javax.swing.text.View;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Clipboard;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

public class enterLogPanelUI {
    // Define a minimum height for the panel
    private static final int MIN_PANEL_HEIGHT = 60;

    public static @NotNull JPanel createLogEntryPanel(String log, String fullLog) {
        // Main rounded panel
        RoundedPanel entryPanel = new RoundedPanel(12, Gray._60);
        entryPanel.setLayout(new BorderLayout());
        entryPanel.setBorder(JBUI.Borders.empty(8, 10));

        // Set minimum size with the full width and our minimum height
        entryPanel.setMinimumSize(new Dimension(100, MIN_PANEL_HEIGHT));

        // Use JTextArea for logLabel to enable text wrapping
        JTextArea logLabel = new JTextArea(log);
        logLabel.setLineWrap(true);
        logLabel.setWrapStyleWord(true);
        logLabel.setEditable(false);
        logLabel.setOpaque(false);
        logLabel.setFont(logLabel.getFont().deriveFont(Font.PLAIN, 12f));
        logLabel.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));

        // Adjust the JTextArea height based on its content
        adjustTextAreaHeight(logLabel, entryPanel);

        final boolean[] isShowingLog = {true};

        // Create the three dots icon as a JLabel
        JLabel optionsLabel = new JLabel(IconLoader.getIcon("AllIcons.Actions.More", enterLogPanelUI.class));
        optionsLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Create the popup menu
        JPopupMenu popupMenu = new JPopupMenu();

        // Add "Toggle" option
        JMenuItem toggleItem = new JMenuItem("Original log");
        toggleItem.addActionListener(e -> {
            if (isShowingLog[0]) {
                logLabel.setText(fullLog);
            } else {
                logLabel.setText(log);
            }
            isShowingLog[0] = !isShowingLog[0];

            // Recalculate the panel size after toggling
            SwingUtilities.invokeLater(() -> {
                adjustTextAreaHeight(logLabel, entryPanel);
                entryPanel.invalidate();
                Container parent = entryPanel.getParent();
                if (parent != null) {
                    parent.validate();
                    parent.repaint();
                }
            });
        });
        popupMenu.add(toggleItem);

        // Add "Show in Logcat" option
        JMenuItem showInLogcatItem = new JMenuItem("Show in Logcat");
        String timestamp = extractTimestamp(log);
        showInLogcatItem.addActionListener(e -> showInLogcat(timestamp));
        popupMenu.add(showInLogcatItem);

        // Add "Copy" option
        JMenuItem copyItem = new JMenuItem("Copy");
        copyItem.addActionListener(e -> {
            if (isShowingLog[0]) {
                copyToClipboard(log);
            } else {
                StringSelection selection = new StringSelection(fullLog);
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(selection, null);
            }
        });
        popupMenu.add(copyItem);

        // Add "Copy Timestamp" option
        JMenuItem copyTimestampItem = new JMenuItem("Copy Timestamp");
        copyTimestampItem.addActionListener(e -> {
            StringSelection selection = new StringSelection(timestamp);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(selection, null);
        });
        popupMenu.add(copyTimestampItem);

        // Show the popup menu when the options label is clicked
        optionsLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                popupMenu.show(optionsLabel, e.getX(), e.getY());
            }
        });

        // Create a wrapper panel to hold (optionsLabel) on the WEST, and (logLabel) in CENTER
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(optionsLabel, BorderLayout.WEST);
        wrapper.add(logLabel, BorderLayout.CENTER);

        // Add the wrapper to entryPanel
        entryPanel.add(wrapper, BorderLayout.CENTER);

        // Set entryPanel sizes for proper layout
        entryPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        // Override the getPreferredSize method to ensure enough height for content
        entryPanel.setPreferredSize(new Dimension(entryPanel.getPreferredSize().width,
                Math.max(MIN_PANEL_HEIGHT,
                        logLabel.getPreferredSize().height + 16)));

        // Add a ComponentListener to adjust the height based on the width
        entryPanel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                adjustTextAreaHeight(logLabel, entryPanel);
            }
        });

        // Add a ComponentListener to adjust the height based on the width
        entryPanel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                adjustTextAreaHeight(logLabel, entryPanel);
            }
        });

        return entryPanel;
    }

    static void adjustTextAreaHeight(JTextArea textArea, JPanel globalPanel) {
        int width = textArea.getWidth();
        if (width > 0) {
            View view = textArea.getUI().getRootView(textArea);
            view.setSize(width, Integer.MAX_VALUE);
            float preferredHeight = view.getPreferredSpan(View.Y_AXIS);
            textArea.setPreferredSize(new Dimension(width, (int) preferredHeight));
            textArea.revalidate();

            // Adjust the size of the global panel
            globalPanel.setPreferredSize(new Dimension(globalPanel.getWidth(), (int) preferredHeight + 16));
            globalPanel.revalidate();
        }
    }

    private static void showFailureAdvice(JTextArea logLabel) {
        String adviceMessage = "<html><div;'>"
                + " Advice: <br>"
                + " * Check your network connection, <br> "
                + "ensure the server is running,<br> "
                + "* Check if the DevKey correct, <br>"
                + "* Verify your credentials.</div></html>";
        logLabel.setToolTipText(adviceMessage);
    }

    // פונקציה להעתקת טקסט ללוח (עם ניסיונות לחלץ UID או אובייקט)
    private static void copyToClipboard(String text) {
        String uid = null;
        String obj = null;
        if (text.contains("UID")) {
            uid = text.substring(text.indexOf("UID") + 4);
        } else if (text.contains("{") && text.contains("}")) {
            obj = text.substring(text.indexOf("{"), text.indexOf("}") + 1);
        }
        StringSelection selection = new StringSelection(uid != null ? uid : obj != null ? obj : text);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(selection, selection);
    }

    // פונקציה לחילוץ טיימסטאמפ מהלוג (לרוב מההתחלה, עד 18 תווים)
    private static String extractTimestamp(String log) {
        if (log != null && log.length() > 18) {
            return log.substring(0, 18).trim();
        }
        return null;
    }

    // פונקציה לקריאה לניווט בלוגקאט לפי טיימסטאמפ
    private static void showInLogcat(String timestamp) {
        LogcatNavigator.navigateToLogcatEntry(timestamp);
    }
}