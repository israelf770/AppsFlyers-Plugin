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
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Clipboard;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

public class enterLogPanelUI {

    private static final Logger log = LoggerFactory.getLogger(enterLogPanelUI.class);

    // פונקציה ליצירת פאנל לוג עם כפתור טוגל מובנה
    public static @NotNull JPanel createLogEntryPanel(String log, String fullLog) {
        // פאנל עגול ראשי
        RoundedPanel entryPanel = new RoundedPanel(12, Gray._60);
        entryPanel.setLayout(new BorderLayout());
        entryPanel.setBorder(JBUI.Borders.empty(8, 10));

        // עטיפת הטקסט ב-HTML לעיטוף עקבי
        String htmlLogShort = "<html><div style='width:300px; white-space:normal; word-wrap:break-word;'>"
                + log + "</div></html>";
        String htmlLogFull = "<html><div style='width:300px; white-space:normal; word-wrap:break-word;'>"
                + fullLog + "</div></html>";

        // יצירת JLabel להצגת הטקסט – ברירת המחדל היא הטקסט הקצר
        JLabel logLabel = new JLabel(htmlLogShort);
        if (log.contains("FAILURE")) {
            logLabel.setForeground(JBColor.RED);
        } else if (log.contains("SUCCESS")) {
            logLabel.setForeground(new JBColor(new Color(0, 128, 0), new Color(0, 200, 0)));
        } else {
            logLabel.setForeground(JBColor.foreground());
        }
        logLabel.setFont(logLabel.getFont().deriveFont(Font.PLAIN, 12f));
        logLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));

        // דגל לשמירת מצב התצוגה (קצר/מלא)
        final boolean[] isShowingLog = { true };

        // יצירת כפתור טוגל (מעוגל) – הוא יהיה תמיד מוצג מצד שמאל
        Icon toggleIcon = IconLoader.getIcon("AllIcons.Actions.SwapPanels", enterLogPanelUI.class);
        RoundedButton toggleButton = new RoundedButton(
                toggleIcon, 15,
                new JBColor(new Color(60, 60, 0, 0), new Color(60,60,0,0)),
                new Dimension(40,30)
        );
        // קביעת מידות קבועות לכפתור
        toggleButton.setPreferredSize(new Dimension(30,30));
        toggleButton.setMinimumSize(new Dimension(30,30));
        toggleButton.setMaximumSize(new Dimension(30,30));
        toggleButton.setActionListener(e -> {
            if (isShowingLog[0]) {
                logLabel.setText(htmlLogFull);
            } else {
                logLabel.setText(htmlLogShort);
            }
            isShowingLog[0] = !isShowingLog[0];
        });

        // יצירת פאנל עטיפה עבור הכפתור טוגל
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setOpaque(false);
        buttonPanel.setPreferredSize(new Dimension(40, 30)); // רוחב קבוע

        // center the button
        buttonPanel.add(Box.createVerticalGlue());
        buttonPanel.add(toggleButton);
        buttonPanel.add(Box.createVerticalGlue());

        // יצירת פאנל עטיפה עם BorderLayout – באזור WEST הכפתור ובאזור CENTER הלייבל
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(buttonPanel, BorderLayout.WEST);
        wrapper.add(logLabel, BorderLayout.CENTER);

        // הוספת הפאנל העטיפה לפאנל הראשי
        entryPanel.add(wrapper, BorderLayout.CENTER);

        // Extract timestamp from log for logcat navigation
        String timestamp = extractTimestamp(log);

        // Panel for action buttons
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        actionPanel.setOpaque(false);

        // Show in Logcat button
        JLabel showInLogcatButton = createActionButton("/icons/logcatIcon.svg", "Show in Logcat");
        showInLogcatButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showInLogcat(timestamp);
                showTemporaryFeedback(showInLogcatButton);
            }
        });

        actionPanel.add(showInLogcatButton);
        actionPanel.setVisible(false); // Initially hidden

        // הוספת מאזין ללחיצה על הפאנל (למעט על הכפתור) לצורך העתקה
        entryPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!buttonPanel.getBounds().contains(e.getPoint())) {
                    if (isShowingLog[0]) {
                        copyToClipboard(log);
                    } else {
                        StringSelection selection = new StringSelection(fullLog);
                        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                        clipboard.setContents(selection, null);
                    }
                    showTemporaryFeedback(entryPanel);
                }
            }
        });


        logLabel.addMouseListener(new MouseAdapter() {
            private Balloon balloon;
            private JComponent balloonContentRef;

            @Override
            public void mouseEntered(MouseEvent e) {
                Icon copyIcon = IconLoader.getIcon("AllIcons.Actions.Copy", getClass());
                JLabel iconLabel = new JLabel(copyIcon);
                JPanel balloonContent = new RoundedPanel(15, new JBColor(Gray._60, Gray._60));
                balloonContent.add(iconLabel);
                balloonContentRef = balloonContent;

                balloon = JBPopupFactory.getInstance()
                        .createBalloonBuilder(balloonContent)
                        .setShowCallout(false)
                        .setAnimationCycle(200)
                        .setFillColor(new JBColor(new Color(0, 0, 0, 0), new Color(0,0,0,0)))
                        .setBorderColor(new JBColor(new Color(60, 60, 0, 0), new Color(60,60,0,0)))
                        .setBorderInsets(JBUI.emptyInsets())
                        .createBalloon();

                int x = logLabel.getWidth() - logLabel.getWidth() - 20;
                int y = logLabel.getHeight() / 2;
                RelativePoint rp = new RelativePoint(logLabel, new Point(x, y));
                balloon.show(rp, Balloon.Position.below);

                actionPanel.setVisible(true);
                entryPanel.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (balloon != null && !balloon.isDisposed()) {
                    balloon.hide();
                }
                Component c = SwingUtilities.getDeepestComponentAt(entryPanel, e.getX(), e.getY());
                if (c == null) {
                    actionPanel.setVisible(false);
                    entryPanel.repaint();
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (balloonContentRef != null) {
                    Color original = balloonContentRef.getBackground();
                    balloonContentRef.setBackground(new JBColor(Gray._100, Gray._100));
                    balloonContentRef.repaint();
                    new Timer(150, evt -> {
                        balloonContentRef.setBackground(original);
                        balloonContentRef.repaint();
                    }).start();
                }
                if (isShowingLog[0]) {
                    copyToClipboard(log);
                } else {
                    StringSelection selection = new StringSelection(fullLog);
                    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                    clipboard.setContents(selection, null);
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


    // מחלקה לכפתור מעוגל המבוססת על RoundedPanel
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

    // פונקציה להעתקת טקסט ללוח
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

    // פונקציה להצגת פידבק ויזואלי קצר (לדוגמה, שינוי רקע זמני)
    private static void showTemporaryFeedback(JComponent component) {
        Color original = component.getBackground();
        component.setBackground(new JBColor(Gray._100, Gray._100));
        component.repaint();
        new Timer(150, evt -> {
            component.setBackground(original);
            component.repaint();
        }).start();
    }

    // מחלקה לפאנל מעוגל (RoundedPanel) כפי שהוגדר בעבר
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
    }
}
