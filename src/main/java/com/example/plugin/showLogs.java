package com.example.plugin;
import com.example.plugin.UI.LogToolWindowFactory;
import com.example.plugin.UI.enterLogPanelUI;
import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class showLogs {
    // Single list to store all logs
    private static final List<String> allLogs = new ArrayList<>();

    // Track current filter
    private static String currentFilter = null;

    /**
     * Get the complete list of logs
     */
    public static List<String> getAllLogs() {
        return allLogs;
    }

    /**
     * Get the current active filter
     */
    public static String getCurrentFilter() {
        return currentFilter;
    }

    /**
     * Check if a log entry matches the current filter
     */
    public static boolean logMatchesFilter(String log, String filter) {
        return filter == null || log.contains("/ " + filter);
    }

    /**
     * Add or update a log entry and refresh the display
     */
    public static void showUpdateLogs(String formattedLogText) {
        if (formattedLogText.equals("new task added: LAUNCH")) {
            // Clear logs when a new LAUNCH task is added
            allLogs.clear();
            refreshLogDisplay();
            return;
        }

        // Only add if not already present to avoid duplicates
        if (!allLogs.contains(formattedLogText)) {
            allLogs.add(formattedLogText);

            // Schedule the UI update after a short delay
            new Thread(() -> {
                try {
                    Thread.sleep(400); // Delay of 400ms
                    refreshLogDisplay();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    /**
     * Set the current filter and refresh the display
     */
    public static void filterLogs(String filterType) {
        // Update the filter and refresh the UI
        currentFilter = filterType;
        refreshLogDisplay();
    }

    /**
     * Helper method to refresh the log display on the UI thread
     */
    private static void refreshLogDisplay() {
        SwingUtilities.invokeLater(LogToolWindowFactory::updateLogContentPanel);
    }
}