package com.example.plugin;
import com.example.plugin.UI.LogEntry;
import com.example.plugin.UI.LogToolWindowFactory;

import javax.swing.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.ArrayList;

import java.util.List;

public class showLogs {
    public static Long lastMillis = null;
    public static Long millis;
    // Single list to store all logs
    private static final List<LogEntry> allLogs = new ArrayList<>();

    // Track current filter
    private static String currentFilter = null;

    // Get the complete list of logs
    public static List<LogEntry> getAllLogs() {
        return allLogs;
    }

    // Get the current active filter
    public static String getCurrentFilter() {
        return currentFilter;
    }

    // Check if a log entry matches the current filter
    public static boolean logMatchesFilter(String log, String filter) {
        return filter == null || log.contains("/ " + filter);
    }

    // Clear all logs from the list
    public static void clearAllLogs() {
        allLogs.clear();
        refreshLogDisplay();
    }

    // Add a log entry and refresh the display immediately
    public static void showUpdateLogs(String shortLog, String type, String fullLog) {
        millis = getTimestampInMillis(shortLog);

        if (lastMillis == null) lastMillis = millis;

        if (!(millis - lastMillis <= 10000) && !shortLog.contains("EVENT")){
            allLogs.clear();
            lastMillis = millis;
        }

        // הוספת הלוג החדש לרשימה
        allLogs.add(new LogEntry(shortLog, fullLog));

        // עדכון מיידי של ה-UI ללא השהיה
        refreshLogDisplay();
    }

    // Set the current filter and refresh the display
    public static void filterLogs(String filterType) {
        // Update the filter and refresh the UI
        currentFilter = filterType;
        refreshLogDisplay();
    }

    // Helper method to refresh the log display on the UI thread
    private static void refreshLogDisplay() {
        SwingUtilities.invokeLater(LogToolWindowFactory::updateLogContentPanel);
    }
    public static long getTimestampInMillis(String logLine) {
        // נניח שהתאריך והשעה (כולל המילישניות) מופיעים בתחילת הלוג - 18 התווים הראשונים.
        String datePart = logLine.substring(0, 18); // לדוגמה: "03-18 07:54:45.073"

        // בניית פורמט שמפרש את המחרוזת עם שנה קבועה כ-1970 כברירת מחדל
        DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                .appendPattern("MM-dd HH:mm:ss.SSS")
                .parseDefaulting(ChronoField.YEAR, 1970)
                .toFormatter();

        LocalDateTime dateTime = LocalDateTime.parse(datePart, formatter);
        return dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
}