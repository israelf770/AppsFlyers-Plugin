package com.example.plugin.UI;

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
        entryPanel.setBorder(JBUI.Borders.empty(8, 10)); // Padding פנימי

        // Create a label to display the log text.
        JLabel logLabel = new JLabel(log);
        logLabel.setForeground(JBColor.foreground());
        logLabel.setFont(logLabel.getFont().deriveFont(Font.PLAIN, 12f));

        // Attach a mouse listener to show a balloon on hover and copy on click.
        logLabel.addMouseListener(new MouseAdapter() {
            private Balloon balloon;
            private JComponent balloonContentRef; // to store a reference to the content


            @Override
            public void mouseEntered(MouseEvent e) {
                Icon copyIcon = IconLoader.getIcon("/icons/copyIcon.svg", getClass());

                // Create a label with only the icon:
                JLabel iconLabel = new JLabel(copyIcon);
                // Create a simple label as balloon content.
                JPanel balloonContent = new RoundedPanel(15, new JBColor(Gray._60, Gray._60));

                balloonContent.add(iconLabel); // או כל רכיב אחר שתרצה להציג
                balloonContentRef = balloonContent;


                balloon = JBPopupFactory.getInstance()
                        .createBalloonBuilder(balloonContent)
                        .setShowCallout(false)
                        .setAnimationCycle(200)
                        .setFillColor(new JBColor(new Color(0, 0, 0, 0), new Color(0,0,0,0)))
                        .setBorderColor(new JBColor(new Color(0, 0, 0, 0), new Color(0,0,0,0)))
                        .setBorderInsets(JBUI.emptyInsets())
                        .createBalloon();


                int x = logLabel.getWidth() - 15;      // horizontal center of the label
                int y = logLabel.getHeight() - 15;         // just below the bottom edge of the label
                RelativePoint rp = new RelativePoint(logLabel, new Point(x, y));

                // Then show the balloon below that point:
                balloon.show(rp, Balloon.Position.below);


            }

            @Override
            public void mouseExited(MouseEvent e) {
                // Start a timer to hide the balloon after a delay
                // timer to delay hiding
                Timer hideTimer = new Timer(300, evt -> {
                    if (balloon != null && !balloon.isDisposed()) {
                        balloon.hide();
                    }
                });
                hideTimer.setRepeats(false);
                hideTimer.start();
            }


            @Override
            public void mouseClicked(MouseEvent e) {
                // When clicked, perform a visual effect on balloonContentRef then copy the text.
                if (balloonContentRef != null) {
                    Color original = balloonContentRef.getBackground();
                    // Set temporary background color to indicate the action.
                    balloonContentRef.setBackground(new JBColor(Gray._100, Gray._100));
                    balloonContentRef.repaint();
                    new Timer(150, evt -> {
                        balloonContentRef.setBackground(original);
                        balloonContentRef.repaint();
                    }).start();
                }
                copyToClipboard(log);
            }
        });

        entryPanel.add(logLabel, BorderLayout.CENTER);

        return entryPanel;
    }

    private static void copyToClipboard(String text) {
        StringSelection selection = new StringSelection(text);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(selection, selection);
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
