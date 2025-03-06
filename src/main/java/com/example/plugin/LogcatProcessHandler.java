package com.example.plugin;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonParseException;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Key;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextArea;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogcatProcessHandler {
    private static final Logger logger = Logger.getInstance(LogcatProcessHandler.class);
    private static long lastTimeStamp = 0;


    // מאגר נתונים לשמירת הלוגים מההרצה הנוכחית.
    private static final Set<String> displayedLogs = new HashSet<>();
    private static final DefaultListModel<String> logListModel = new DefaultListModel<>();

    // משתנה סטטי לשמירת ה-JDialog והקומפוננטות שלו.
    public static JDialog dialog;
    private static JPanel logPanel;
    private static JScrollPane scrollPane;

    // משתנה סטטי לשמירת תהליך הלוגקט – אם תהליך רץ, לא נתחיל חדש.
    private static OSProcessHandler logcatProcessHandler = null;

    // קבועים לעיצוב הממשק.
    private static final JBColor BACKGROUND_COLOR = new JBColor(new Color(30, 30, 30), new Color(30, 30, 30));
    private static final JBColor ENTRY_BACKGROUND_COLOR = new JBColor(new Color(246, 241, 241), new Color(50, 50, 50));
    private static final JBColor TEXT_AREA_BACKGROUND_COLOR = new JBColor(new Color(255, 255, 255), new Color(40, 40, 40));
    private static final JBColor COPY_BUTTON_COLOR = new JBColor(new Color(0, 122, 255), new Color(0, 122, 255));
    private static final Font TEXT_AREA_FONT = new Font("Arial", Font.PLAIN, 14);
    private static final Font BUTTON_FONT = new Font("Arial", Font.BOLD, 12);

    /**
     * מתחילים את קריאת logcat.
     */
    public static void startLogcat() {
        try {
            // ננקה את הלוגים מההרצה הקודמת.
            logListModel.clear();
            displayedLogs.clear();

            // אם הדיאלוג קיים והוא מוצג – נסתיר אותו.
            if (dialog != null && dialog.isVisible()) {
                dialog.setVisible(false);
            }
            logger.info("logcat listener started");

            ProcessBuilder builder = new ProcessBuilder("adb", "logcat", "*:V");
            Process process = builder.start();
            OSProcessHandler processHandler = getOsProcessHandler(process);
            processHandler.startNotify();
        } catch (Exception e) {
            logger.error("Error starting adb logcat", e);
        }
    }

    /**
     * יוצר את ה-OSProcessHandler ומאזין ללוגים.
     */
    private static @NotNull OSProcessHandler getOsProcessHandler(Process process) {
        OSProcessHandler processHandler = new OSProcessHandler(process, "adb logcat", StandardCharsets.UTF_8);
        processHandler.addProcessListener(new ProcessAdapter() {
            @Override
            public void onTextAvailable(@NotNull ProcessEvent event, @NotNull Key outputType) {
                String text = event.getText();

                // מחשבים את הטיימסטמפ מהלוג עצמו.
                SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm:ss.SSS");
                Pattern datePattern = Pattern.compile("^\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\.\\d{3}");
                Matcher dateMatcher = datePattern.matcher(text);
                String timestamp = "";
                long timestampMillis = 0;
                if (dateMatcher.find()) {
                    timestamp = dateMatcher.group();
                    try {
                        Date date = sdf.parse(timestamp);
                        timestampMillis = date.getTime();
                    } catch (ParseException e) {
                        logger.error("Error parsing timestamp: " + e.getMessage(), e);
                    }
                }

                // דוגמה לבדיקה: מעבדים רק לוגים שנוצרו אחרי זמן ההרצה (sessionStartTime)
                if (timestampMillis >= lastTimeStamp) {
                    if (text.contains("CONVERSION-") || text.contains("LAUNCH-")) {
                        if (text.contains("SUCCESS") || text.contains("FAILURE")) {
                            Pattern pattern = Pattern.compile("result:\\s*\\w+");
                            Matcher matcher = pattern.matcher(text);
                            if (matcher.find()) {
                                String result = matcher.group();
                                String logMessage = timestamp + " - " + result;
                                SwingUtilities.invokeLater(() -> showDialog(logMessage));
                            }
                        }
                        String formattedLog = extractKeyValueFromLog(text, timestamp);
                        if (formattedLog != null) {
                            SwingUtilities.invokeLater(() -> showDialog(formattedLog));
                        } else {
                            logger.warn("No timestamp found in log: " + text);
                        }
                    }
                    lastTimeStamp = timestampMillis;
                }
            }
        });
        return processHandler;
    }

    /**
     * מעדכנת את הדיאלוג עם הלוגים.
     */
    private static void showDialog(String formattedLogText) {
        // אם הלוג לא קיים עדיין, נוסיף אותו.
        if (!displayedLogs.contains(formattedLogText)) {
            logListModel.addElement(formattedLogText);
        }

        // אם הדיאלוג לא קיים – ניצור אותו.
        if (dialog == null) {
            createDialog();
        } else {
            // אם הוא כבר קיים – נוודא שהוא מוצג
            dialog.setVisible(true);
        }

        // עדכון הפאנל עם רשימת הלוגים.
        updateLogPanel();
    }

    // יוצר את הדיאלוג (JDialog) להצגת הלוגים.

    private static void createDialog() {
        // פאנל ראשי עם BorderLayout.
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BACKGROUND_COLOR);

        // פאנל כפתורים בחלק העליון.
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setBackground(BACKGROUND_COLOR);
//        JButton closeButton = createCloseButton();
//        buttonPanel.add(closeButton);

        // פאנל ללוגים.
        logPanel = new JPanel();
        logPanel.setLayout(new BoxLayout(logPanel, BoxLayout.Y_AXIS));
        logPanel.setBackground(BACKGROUND_COLOR);

        // הוספת פאנל הכפתורים לראש הפאנל הראשי.
        mainPanel.add(buttonPanel, BorderLayout.NORTH);

        // הוספת פאנל הלוגים בתוך ScrollPane.
        scrollPane = new JBScrollPane(logPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        scrollPane.setPreferredSize(new Dimension(400, 300));
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // יצירת JDialog לא מודאלי והגדרת התוכן.
        dialog = new JDialog((Frame) null, "Extracted Log", false);
        dialog.getContentPane().add(mainPanel);
        dialog.pack();
        dialog.setLocationRelativeTo(null); // מרכז על המסך
        dialog.setVisible(true);
    }


    //מעדכן את הפאנל עם כל הלוגים.

    private static void updateLogPanel() {
        logPanel.removeAll();
        for (int i = 0; i < logListModel.size(); i++) {
            String log = logListModel.get(i);
            if (!displayedLogs.contains(log)) {
                JPanel entryPanel = createLogEntryPanel(log);
                logPanel.add(entryPanel);
                logPanel.add(Box.createVerticalStrut(10));
            }// רווח בין רשומות.
        }
        logPanel.revalidate();
        logPanel.repaint();
    }

    /**
     * יוצר פאנל לרשומת לוג בודדת.
     */
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

        JButton copyButton = createCopyButton(log);
        entryPanel.add(new JBScrollPane(logTextArea), BorderLayout.CENTER);
        entryPanel.add(copyButton, BorderLayout.SOUTH);
        entryPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        entryPanel.setPreferredSize(new Dimension(180, 90));

        return entryPanel;
    }

    /**
     * יוצר כפתור Copy.
     */
    private static JButton createCopyButton(String log) {
        JButton copyButton = new JButton("Copy");
        copyButton.setBackground(COPY_BUTTON_COLOR);
        copyButton.setFont(BUTTON_FONT);
        copyButton.setFocusPainted(false);
        copyButton.setPreferredSize(new Dimension(100, 30));
        copyButton.addActionListener(e -> {
            copyToClipboard(log);
            Color originalColor = copyButton.getBackground();
            copyButton.setBackground(JBColor.green);
            new Timer(500, evt -> copyButton.setBackground(originalColor)).start();
        });
        return copyButton;
    }

    /**
     * מעתיק את הלוג ללוח.
     */
    private static void copyToClipboard(String log) {
        StringSelection selection = new StringSelection(log);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(selection, selection);
    }

    /**
     * יוצר כפתור סגירה.
     */
//    private static JButton createCloseButton() {
//        JButton closeButton = new JButton("✖");
//        closeButton.setFont(new Font("Arial", Font.BOLD, 16));
//        closeButton.setFocusPainted(false);
//        closeButton.setBorder(BorderFactory.createEmptyBorder());
//        closeButton.setPreferredSize(new Dimension(30, 30));
//        closeButton.setMaximumSize(new Dimension(30, 30));
//        closeButton.setMinimumSize(new Dimension(30, 30));
//        closeButton.setAlignmentX(Component.LEFT_ALIGNMENT);
//        closeButton.addActionListener(e -> closeDialog());
//        return closeButton;
//    }
//
//    /**
//     * מסגור (הסתרה) של הדיאלוג מבלי לאבד את הנתונים.
//     */
//    private static void closeDialog() {
//        if (dialog != null) {
//            dialog.setVisible(false);
//        }
//    }

    /**
     * פונקציה לחילוץ נתונים מהלוג, המקבלת את הטיימסטמפ כחלק מהפורמט.
     */
    private static String extractKeyValueFromLog(String logText, String timestamp) {
        try {
            int jsonStartIndex = logText.indexOf("{");
            int jsonEndIndex = logText.lastIndexOf("}");

            if (jsonStartIndex != -1 && jsonEndIndex != -1) {
                String jsonPart = logText.substring(jsonStartIndex, jsonEndIndex + 1);
                if (jsonPart.chars().filter(ch -> ch == '{').count() != jsonPart.chars().filter(ch -> ch == '}').count()) {
                    logger.warn("Incomplete JSON: " + jsonPart);
                    return null;
                }
                logger.info("JSON Part: " + jsonPart);

                JsonObject jsonObject = JsonParser.parseString(jsonPart).getAsJsonObject();
                String uid = jsonObject.has("uid") ? jsonObject.get("uid").getAsString() : "UID Not Found";
                return timestamp + " - UID: " + uid;
            } else {
                return null;
            }
        } catch (JsonParseException e) {
            logger.error("Error parsing JSON from log: " + e.getMessage() + ", jsonPart=" + logText, e);
            return null;
        } catch (Exception e) {
            logger.error("Error extracting key-value from log", e);
            return null;
        }
    }
}
