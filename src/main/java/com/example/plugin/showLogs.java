package com.example.plugin;
import com.example.plugin.UI.LogToolWindowFactory;
import com.example.plugin.UI.enterLogPanelUI;
import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class showLogs {
    private static final List<String> displayedLogs = new ArrayList<>();
    private static final JPanel logPanel = new JPanel();

    public static List<String> getDisplayedLogs() {
        return displayedLogs;
    }

    public static void showUpdateLogs(String formattedLogText) {
        if (formattedLogText.equals("new task added: LAUNCH")) {
            displayedLogs.clear();
            return;
        } else if (!displayedLogs.contains(formattedLogText)) {
            displayedLogs.add(formattedLogText);
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

        logPanel.removeAll();

        for (String log : displayedLogs) {
            // If no filter or log contains the filter type, add it
            if (filterType == null || log.contains("/ " + filterType)) {
                JPanel entryPanel = enterLogPanelUI.createLogEntryPanel(log);
                logPanel.add(entryPanel);
                logPanel.add(Box.createVerticalStrut(10));
            }
        }

        logPanel.revalidate();
        logPanel.repaint();
    }
}