package com.example.plugin.actions;

import com.example.plugin.showLogs;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;
import javax.swing.Icon;
import com.intellij.openapi.util.IconLoader;

/**
 * Action that filters logs to show only event-related entries.
 */
public class ShowEventAction extends AnAction {
    public static class MyClass {
        public static final Icon MY_ICON = IconLoader.getIcon("/icons/eventIcon.svg", MyClass.class);
    }

    public ShowEventAction() {
        super("EVENT", "Show event logs", MyClass.MY_ICON);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        showLogs.filterLogs("EVENT");
    }
}