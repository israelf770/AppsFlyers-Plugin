package com.example.plugin;
import com.example.plugin.UI.LogToolWindowFactory;
import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class showLogs {
    private static final List<String> displayedLogs = new ArrayList<>();
    private static final List<String> filteredLogs = new ArrayList<>(); // To hold currently filtered logs
    private static String currentFilter = null; // Track current filter

    public static List<String> getDisplayedLogs() {
        return displayedLogs;
    }

    public static List<String> getFilteredLogs() {
        return filteredLogs;
    }

    public static String getCurrentFilter() {
        return currentFilter;
    }

    public static void showUpdateLogs(String formattedLogText) {
        if (formattedLogText.equals("new task added: LAUNCH")) {
            displayedLogs.clear();
            filteredLogs.clear(); // Clear filtered logs too
            return;
        } else if (!displayedLogs.contains(formattedLogText)) {
            displayedLogs.add(formattedLogText);

            // If we have an active filter, check if this log should be added to filtered logs
            if (currentFilter == null || formattedLogText.contains("/ " + currentFilter)) {
                filteredLogs.add(formattedLogText);
            }
        }

        new Thread(() -> {
            try {
                Thread.sleep(400); // Delay of 400ms
                SwingUtilities.invokeLater(LogToolWindowFactory::updateLogContentPanel); // Call updateLogPanel on UI Thread
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static void filterLogs(String filterType) {
        currentFilter = filterType; // Store current filter
        filteredLogs.clear(); // Clear previous filtered logs

        // Create new filtered list based on current filter
        for (String log : displayedLogs) {
            if (filterType == null || log.contains("/ " + filterType)) {
                filteredLogs.add(log);
            }
        }

        // Update the UI with filtered logs
        SwingUtilities.invokeLater(LogToolWindowFactory::updateLogContentPanel);
    }
}