package com.example.plugin.UI;

import com.example.plugin.actions.*;
import com.example.plugin.showLogs;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.Separator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.Gray;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

public class LogToolWindowFactory implements ToolWindowFactory {

    // Reference to the log panel for updates
    private static JPanel logPanel;

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        // Build main panel to contain all content
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Gray._30);

        Content content = ContentFactory.getInstance().createContent(mainPanel, "", false);
        toolWindow.getContentManager().addContent(content);

        // Actions for the tab header
        AnAction showAllAction = new ShowAllAction();
        AnAction showConversionAction = new ShowConversionAction();
        AnAction showEventAction = new ShowEventAction();
        AnAction showLaunchAction = new ShowLaunchAction();
        AnAction RunAction = new RunAction();

        toolWindow.setTitleActions(Arrays.asList(
                RunAction,
                new Separator(),
                showAllAction,
                new Separator(),
                showConversionAction,
                new Separator(),
                showEventAction,
                new Separator(),
                showLaunchAction
        ));

        // Log panel
        logPanel = new JPanel();
        logPanel.setLayout(new BoxLayout(logPanel, BoxLayout.Y_AXIS));
        logPanel.setBackground(Gray._30);

        JScrollPane scrollPane = new JScrollPane(logPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        scrollPane.setPreferredSize(new Dimension(600, 400));
        mainPanel.add(scrollPane, BorderLayout.CENTER);
    }

    //Updates the log panel with logs that match the current filter

    public static void updateLogContentPanel() {
        if (logPanel != null) {
            logPanel.removeAll();

            String currentFilter = showLogs.getCurrentFilter();

            // Iterate through all logs and add only those that match the current filter
            for (String log : showLogs.getAllLogs()) {
                if (showLogs.logMatchesFilter(log, currentFilter)) {
                    JPanel entryPanel = enterLogPanelUI.createLogEntryPanel(log);
                    logPanel.add(entryPanel);
                    logPanel.add(Box.createVerticalStrut(10)); // Space between entries
                }
            }

            logPanel.revalidate();
            logPanel.repaint();
        }
    }
}