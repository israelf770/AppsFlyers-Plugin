package com.example.plugin;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.util.Key;
import com.intellij.ui.Gray;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextArea;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

public class LogcatProcessHandler {
    private static final Logger logger = Logger.getInstance(LogcatProcessHandler.class);

    // משתנה לשמירת הלוגים שהוצגו כבר
    private static final Set<String> displayedLogs = new HashSet<>();

    public static void startLogcat() {
        try {
            logger.info("logcat listener started");
            ProcessBuilder builder = new ProcessBuilder("adb", "logcat", "*:V");
            Process process = builder.start();
            OSProcessHandler processHandler = getOsProcessHandler(process);
            processHandler.startNotify();
        } catch (Exception e) {
            logger.error("Error starting adb logcat", e);
        }
    }

    private static @NotNull OSProcessHandler getOsProcessHandler(Process process) {
        OSProcessHandler processHandler = new OSProcessHandler(process, "adb logcat", StandardCharsets.UTF_8);
        processHandler.addProcessListener(new ProcessAdapter() {
            @Override
            public void onTextAvailable(@NotNull ProcessEvent event, @NotNull Key outputType) {
                String text = event.getText();
                if (text.contains("CONVERSION-") || text.contains("LAUNCH-")) {
                    String formattedLog = extractKeyValueFromLog(text);
                    if (formattedLog != null) {
                        showPopup(formattedLog);
                    }
                }
            }
        });
        return processHandler;
    }

    private static final DefaultListModel<String> logListModel = new DefaultListModel<>();

    private static void showPopup(String formattedLogText) {
        // אם הלוג כבר הוצג, לא נוסיף אותו שוב
        if (!displayedLogs.contains(formattedLogText)) {
            displayedLogs.add(formattedLogText);  // הוספת הלוג שהוצג לרשימה
            logListModel.addElement(formattedLogText); // הוספת הלוג לרשימה שמופיעה בחלון
        }

        JPanel logPanel = new JPanel();
        logPanel.setLayout(new BoxLayout(logPanel, BoxLayout.Y_AXIS));
        logPanel.setBackground(Gray._245); // רקע בהיר יותר

        for (int i = 0; i < logListModel.size(); i++) {
            String log = logListModel.get(i);

            JPanel entryPanel = new JPanel(new BorderLayout());
            entryPanel.setBackground(JBColor.WHITE); // רקע בהיר לכל לוג

            JTextArea logTextArea = new JBTextArea(log);
            logTextArea.setFont(new Font("Arial", Font.PLAIN, 14)); // גודל פונטים קריא
            logTextArea.setWrapStyleWord(true);
            logTextArea.setLineWrap(true);
            logTextArea.setEditable(false);
            logTextArea.setBackground(Gray._240); // צבע רקע בהיר לפסקה

            JButton copyButton = new JButton("Copy");
            copyButton.setBackground(new JBColor(new Color(0, 122, 255), new Color(0,122,255))); // צבע כחול לכפתור
            copyButton.setForeground(JBColor.black); // צבע טקסט לבן
            copyButton.setFont(new Font("Arial", Font.BOLD, 12)); // גודל ופונט לכפתור
            copyButton.setFocusPainted(false); // להסיר את הצבע הכחול כשיש התמקדות
            copyButton.setBorder(BorderFactory.createLineBorder(new JBColor(new Color(0, 122, 255), new Color(0,122,255)))); // גבול לכפתור

            // הוספת אקשן לכפתור ההעתקה
            copyButton.addActionListener(e -> {
                StringSelection selection = new StringSelection(log);
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(selection, selection);
            });

            entryPanel.add(new JBScrollPane(logTextArea), BorderLayout.CENTER);
            entryPanel.add(copyButton, BorderLayout.EAST);

            // הוספת מימדים לגורמים בתוך הלוג
            entryPanel.setPreferredSize(new Dimension(500, 70));
            logPanel.add(entryPanel);
            logPanel.add(Box.createVerticalStrut(10)); // מרווח בין לוגים
        }

        JScrollPane scrollPane = new JBScrollPane(logPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // גבולות פנימיים

        JBPopup popup = JBPopupFactory.getInstance()
                .createComponentPopupBuilder(scrollPane, null)
                .setTitle("Extracted Log")
                .setMovable(true)
                .setResizable(true)
                .setRequestFocus(true)
                .createPopup();

        SwingUtilities.invokeLater(popup::showInFocusCenter);
    }

    private static String extractKeyValueFromLog(String logText) {
        try {
            // חיפוש המיקום של ה-JSON בטקסט
            int jsonStartIndex = logText.indexOf("{");
            int jsonEndIndex = logText.lastIndexOf("}");

            if (jsonStartIndex != -1 && jsonEndIndex != -1) {
                // חיתוך ה-JSON מתוך הלוג
                String jsonPart = logText.substring(jsonStartIndex, jsonEndIndex + 1);

                // פרס את ה-JSON
                JsonObject jsonObject = JsonParser.parseString(jsonPart).getAsJsonObject();

                // חיפוש המפתח 'uid' מתוך ה-JSON
                String uid = jsonObject.has("uid") ? jsonObject.get("uid").getAsString() : "UID Not Found";

                // החזרת התוצאה
                return "UID: " + uid;
            } else {
                return null;
            }
        } catch (Exception e) {
            logger.error("Error extracting key-value from log", e);
            return null;
        }
    }
}
