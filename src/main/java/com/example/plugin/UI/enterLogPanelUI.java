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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import javax.swing.border.Border;
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

        // Prepare short/full HTML with improved text wrapping
        String htmlLogShort = "<html><div style='width:100%; white-space:pre-wrap; word-wrap:break-word;'>"
                + log + "</div></html>";
        String htmlLogFull = "<html><div style='width:100%; white-space:pre-wrap; word-wrap:break-word;'>"
                + fullLog + "</div></html>";

        // Label with default short text
        JLabel logLabel = new JLabel(htmlLogShort);
        logLabel.setVerticalAlignment(SwingConstants.TOP);

        // Ensure the label resizes properly
        logLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        logLabel.setPreferredSize(new Dimension(100, MIN_PANEL_HEIGHT - 16)); // Account for panel padding

        if (log.contains("FAILURE")||log.contains("No deep link detected")) {
            logLabel.setForeground(JBColor.RED);
        } else if (log.contains("SUCCESS")||log.contains("deepLink")) {
            logLabel.setForeground(new JBColor(new Color(0, 128, 0), new Color(0, 200, 0)));
        } else {
            logLabel.setForeground(JBColor.foreground());
        }

        logLabel.setFont(logLabel.getFont().deriveFont(Font.PLAIN, 12f));
        logLabel.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));

        final boolean[] isShowingLog = {true};

        // 1) Create the toggle button
        Icon toggleIcon = IconLoader.getIcon("AllIcons.Actions.SwapPanels", enterLogPanelUI.class);
        RoundedButton toggleButton = new RoundedButton(
                toggleIcon, 15,
                new JBColor(new Color(60, 60, 0, 0), new Color(60, 60, 0, 0)),
                new Dimension(30, 30)
        );

        toggleButton.setActionListener(e -> {
            if (isShowingLog[0]) {
                logLabel.setText(htmlLogFull);
            } else {
                logLabel.setText(htmlLogShort);
            }
            isShowingLog[0] = !isShowingLog[0];

            // Recalculate the panel size after toggling
            SwingUtilities.invokeLater(() -> {
                entryPanel.invalidate();
                Container parent = entryPanel.getParent();
                if (parent != null) {
                    parent.validate();
                    parent.repaint();
                }
            });
        });

        // 2) Create the "Show in Logcat" button
        JLabel showInLogcatButton = createActionButton(); // the method returning a JLabel with logcatIcon
        String timestamp = extractTimestamp(log);         // get timestamp for showInLogcat
        showInLogcatButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showInLogcat(timestamp);
            }
        });

        // 3) Put BOTH buttons in a single panel with horizontal layout
        JPanel leftButtonsPanel = new JPanel();
        leftButtonsPanel.setLayout(new BoxLayout(leftButtonsPanel, BoxLayout.X_AXIS));
        leftButtonsPanel.setOpaque(false);

        // Add the toggle button, some spacing, then the logcat button
        leftButtonsPanel.add(toggleButton);
        leftButtonsPanel.add(Box.createHorizontalStrut(5));
        leftButtonsPanel.add(showInLogcatButton);

        // 4) Create a wrapper panel to hold (leftButtonsPanel) on the WEST, and (logLabel) in CENTER
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(leftButtonsPanel, BorderLayout.WEST);
        wrapper.add(logLabel, BorderLayout.CENTER);

        // Add the wrapper to entryPanel
        entryPanel.add(wrapper, BorderLayout.CENTER);

        // Set entryPanel sizes for proper layout
        entryPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        // Override the getPreferredSize method to ensure enough height for content
        entryPanel.setPreferredSize(new Dimension(entryPanel.getPreferredSize().width,
                Math.max(MIN_PANEL_HEIGHT,
                        logLabel.getPreferredSize().height + 16)));

        // 5) MouseListener on the entryPanel for copying text if clicked outside the buttons
        logLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Convert the click point to the leftButtonsPanel coordinate space
                Point pt = SwingUtilities.convertPoint(entryPanel, e.getPoint(), leftButtonsPanel);
                if (!leftButtonsPanel.contains(pt)) {
                    if (isShowingLog[0]) {
                        copyToClipboard(log);
                    } else {
                        StringSelection selection = new StringSelection(fullLog);
                        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                        clipboard.setContents(selection, null);
                    }
                }
            }
        });

        logLabel.addMouseListener(new MouseAdapter() {
            private Balloon balloon;
            @Override
            public void mouseEntered(MouseEvent e) {
                Icon copyIcon = IconLoader.getIcon("AllIcons.Actions.Copy", getClass());
                JLabel iconLabel = new JLabel(copyIcon);
                JPanel balloonContent = new RoundedPanel(8, new JBColor(Gray._60, Gray._60));
                balloonContent.setLayout(new BorderLayout());
                balloonContent.add(iconLabel, BorderLayout.CENTER);
                balloonContent.add(iconLabel);
                balloon = JBPopupFactory.getInstance()
                        .createBalloonBuilder(balloonContent)
                        .setShowCallout(false)
                        .setAnimationCycle(200)
                        .setBorderInsets(JBUI.emptyInsets())
                        .createBalloon();
                int x = logLabel.getWidth() - logLabel.getWidth() - 70;
                int y = logLabel.getHeight() / 2;
                RelativePoint rp = new RelativePoint(logLabel, new Point(x, y));
                balloon.show(rp, Balloon.Position.below);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                if (balloon != null && !balloon.isDisposed()) {
                    balloon.hide();
                }
            }
        });
        return entryPanel;
    }

    // פונקציה ליצירת כפתורי פעולה (עם אייקון ו-tooltip)
    private static JLabel createActionButton() {
        Icon icon = IconLoader.getIcon("/icons/logcatIcon.svg", enterLogPanelUI.class);
        JLabel button = new JLabel(icon);
        button.setToolTipText("Show in Logcat");
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
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

    // מחלקה פנימית לציור פאנל מעוגל
    public static class RoundedPanel extends JPanel {
        private final int cornerRadius;
        private final Color backgroundColor;

        public RoundedPanel(int cornerRadius, Color backgroundColor) {
            this.cornerRadius = cornerRadius;
            this.backgroundColor = backgroundColor;
            setOpaque(false);
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

        @Override
        public Dimension getPreferredSize() {
            Dimension size = super.getPreferredSize();
            return new Dimension(size.width, Math.max(MIN_PANEL_HEIGHT, size.height));
        }
    }

    // מחלקה פנימית לכפתור מעוגל המבוסס על RoundedPanel
    public static class RoundedButton extends RoundedPanel {
        private ActionListener actionListener;

        public RoundedButton(Icon icon, int cornerRadius, Color backgroundColor, Dimension size) {
            super(cornerRadius, backgroundColor);
            setPreferredSize(size);
            setLayout(new BorderLayout());
            JLabel iconLabel = new JLabel(icon);
            iconLabel.setHorizontalAlignment(JLabel.CENTER);
            iconLabel.setVerticalAlignment(JLabel.CENTER);
            add(iconLabel, BorderLayout.CENTER);
            setBorder(BorderFactory.createEmptyBorder());
            setOpaque(false);
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (actionListener != null) {
                        actionListener.actionPerformed(new ActionEvent(
                                RoundedButton.this,
                                ActionEvent.ACTION_PERFORMED,
                                "clicked"
                        ));
                    }
                }
                @Override
                public void mouseEntered(MouseEvent e) {
                    setBackground(backgroundColor.brighter());
                    repaint();
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    setBackground(backgroundColor);
                    repaint();
                }
            });
        }

        public void setActionListener(ActionListener listener) {
            this.actionListener = listener;
        }
    }
}