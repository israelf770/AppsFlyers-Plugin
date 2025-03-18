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
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Clipboard;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

public class enterLogPanelUI {

    public static @NotNull JPanel createLogEntryPanel(String log) {
        // Create a rounded panel for the log entry.
        RoundedPanel entryPanel = new RoundedPanel(12, Gray._60);
        entryPanel.setLayout(new BorderLayout());
        entryPanel.setBorder(JBUI.Borders.empty(8, 10));

        // Create a label to display the log text.
        JLabel logLabel = new JLabel(log);
        logLabel.setForeground(JBColor.foreground());
        logLabel.setFont(logLabel.getFont().deriveFont(Font.PLAIN, 12f));

        // Extract timestamp from log for logcat navigation
        String timestamp = extractTimestamp(log);

        // Panel for action buttons
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        actionPanel.setOpaque(false);

        // Copy button
        JLabel copyButton = createActionButton("/icons/copyIcon.svg", "Copy to clipboard");
        copyButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                copyToClipboard(log);
                showTemporaryFeedback(copyButton);
            }
        });

        // Show in Logcat button
        JLabel showInLogcatButton = createActionButton("/icons/logcatIcon.svg", "Show in Logcat");
        showInLogcatButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showInLogcat(timestamp);
                showTemporaryFeedback(showInLogcatButton);
            }
        });

        actionPanel.add(copyButton);
        actionPanel.add(showInLogcatButton);
        actionPanel.setVisible(false); // Initially hidden


        entryPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                actionPanel.setVisible(true);
                entryPanel.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                // Check if cursor is still within the panel or its children
                Component c = SwingUtilities.getDeepestComponentAt(entryPanel, e.getX(), e.getY());
                if (c == null) {
                    actionPanel.setVisible(false);
                    entryPanel.repaint();
                }
            }
        });

        entryPanel.add(logLabel, BorderLayout.CENTER);
        entryPanel.add(actionPanel, BorderLayout.EAST);

        return entryPanel;
    }

    private static JLabel createActionButton(String iconPath, String tooltipText) {
        Icon icon = IconLoader.getIcon(iconPath, enterLogPanelUI.class);
        JLabel button = new JLabel(icon);
        button.setToolTipText(tooltipText);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private static void showTemporaryFeedback(JComponent component) {
        Color original = component.getBackground();
        component.setBackground(new JBColor(Gray._100, Gray._100));
        component.repaint();
        new Timer(150, evt -> {
            component.setBackground(original);
            component.repaint();
        }).start();
    }

   private static void copyToClipboard(String text) {
       String uid = null;
       String obj = null;
       if(text.contains("UID")) {
           uid = text.substring(text.indexOf("UID")+4);
       } else if (text.contains("{") && text.contains("}")) {
           obj = text.substring(text.indexOf("{"), text.indexOf("}")+1);
       }
       StringSelection selection = new StringSelection(uid != null ? uid : obj != null ? obj : text);
       Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
       clipboard.setContents(selection, selection);
   }

    private static String extractTimestamp(String log) {
        // Extract timestamp from the beginning of the log
        // Format is typically at the beginning, up to 14 characters
        if (log != null && log.length() > 18) {
            return log.substring(0, 18).trim();
        }
        return null;
    }

    private static void showInLogcat(String timestamp) {
        // Call the method in new LogcatNavigator class
        LogcatNavigator.navigateToLogcatEntry(timestamp);
    }

    // מחלקה פנימית לציור פאנל מעוגל
    public static class RoundedPanel extends JPanel {
        private final int cornerRadius;
        private final Color backgroundColor;

        public RoundedPanel(int cornerRadius, Color backgroundColor) {
            this.cornerRadius = cornerRadius;
            this.backgroundColor = backgroundColor;
            setOpaque(false); // לציור ידני
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Shape round = new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);
            g2.setColor(backgroundColor);
            g2.fill(round);

            g2.dispose();
            super.paintComponent(g);
        }
    }
}
