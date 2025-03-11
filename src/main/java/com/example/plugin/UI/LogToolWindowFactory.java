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

    // נשמור הפניה לפאנל הלוגים לצורך עדכון
    private static JPanel logPanel;

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        // נבנה פאנל ראשי שמכיל את כל התוכן
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Gray._30);

        Content content = ContentFactory.getInstance().createContent(mainPanel, "", false);
        toolWindow.getContentManager().addContent(content);

        // פעולות לכותרת הטאב
        AnAction showAllAction = new ShowAllAction();
        AnAction showConversionAction = new ShowConversionAction();
        AnAction showEventAction = new ShowEventAction();
        AnAction showLaunchAction = new ShowLaunchAction();
        AnAction RunAction = new RunAction();

        toolWindow.setTitleActions(Arrays.asList(
                RunAction,
                new Separator(),        // מפריד לפני הכפתורים הבאים
                showAllAction,
                new Separator(),        // מפריד לפני הכפתורים הבאים
                showConversionAction,
                new Separator(),        // מפריד לפני הכפתורים הבאים
                showEventAction,
                new Separator(),        // מפריד לפני הכפתורים הבאים
                showLaunchAction
        ));

        // פאנל הלוגים
        logPanel = new JPanel();
        logPanel.setLayout(new BoxLayout(logPanel, BoxLayout.Y_AXIS));
        logPanel.setBackground(Gray._30);

        JScrollPane scrollPane = new JScrollPane(logPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        scrollPane.setPreferredSize(new Dimension(600, 400));
        mainPanel.add(scrollPane, BorderLayout.CENTER);
    }

    // מתודה לעדכון תוכן הלוגים בתוך הטאב
    public static void updateLogContentPanel() {
        if (logPanel != null) {
            logPanel.removeAll();
            for (String log : showLogs.getDisplayedLogs()) {
                JPanel entryPanel = enterLogPanelUI.createLogEntryPanel(log);
                logPanel.add(entryPanel);
                logPanel.add(Box.createVerticalStrut(10)); // רווח בין רשומות
            }
            logPanel.revalidate();
            logPanel.repaint();
        }
    }

}
