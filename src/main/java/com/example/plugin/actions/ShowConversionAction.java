package com.example.plugin.actions;

import com.example.plugin.UI.LogToolWindowFactory;
import com.example.plugin.showLogs;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.util.IconLoader;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class ShowConversionAction extends AnAction {
    public static class MyClass {
        public static final Icon MY_ICON = IconLoader.getIcon("/icons/conversionIcon.svg", RunAction.MyClass.class);
    }
    public ShowConversionAction() {
        super("CONVERSION", "Show conversion logs", MyClass.MY_ICON); // טקסט הכפתור
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        // הקריאה לפונקציה שסיננת קודם:
        showLogs.filterLogs("conversion");
        LogToolWindowFactory.updateLogContentPanel(); // Add this line to refresh the UI
    }
}
