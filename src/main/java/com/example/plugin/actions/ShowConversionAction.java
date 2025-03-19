package com.example.plugin.actions;

import com.example.plugin.showLogs;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.util.IconLoader;
import org.jetbrains.annotations.NotNull;
import javax.swing.Icon;

/**
 * Action that filters logs to show only conversion-related entries.
 */
public class ShowConversionAction extends AnAction {
    public static class MyClass {
        public static final Icon MY_ICON = IconLoader.getIcon("/icons/conversionIcon.svg", RunAction.MyClass.class);
    }

    public ShowConversionAction() {
        super("CONVERSION", "Show conversion logs", MyClass.MY_ICON);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        showLogs.filterLogs("CONVERSION");
    }
}