package com.example.plugin.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;
import com.example.plugin.showLogs;
import javax.swing.Icon;
import com.intellij.openapi.util.IconLoader;

/**
 * Action that shows all logs without filtering.
 */
public class ShowAllAction extends AnAction {
    public static class MyClass {
        public static final Icon MY_ICON = IconLoader.getIcon("/icons/allIcon.svg", MyClass.class);
    }

    public ShowAllAction() {
        super("All", "Show all logs", MyClass.MY_ICON);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        showLogs.filterLogs(null);
    }
}