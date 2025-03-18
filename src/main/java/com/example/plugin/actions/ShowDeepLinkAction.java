package com.example.plugin.actions;

import com.example.plugin.showLogs;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.util.IconLoader;
import org.jetbrains.annotations.NotNull;

import javax.swing.Icon;

public class ShowDeepLinkAction extends AnAction {
    public static class MyClass {
        public static final Icon MY_ICON = IconLoader.getIcon("AllIcons.Actions.InlayGlobe", MyClass.class);
    }

    public ShowDeepLinkAction() {
        super("DEEPLINK", "Show deep link logs", MyClass.MY_ICON);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        showLogs.filterLogs("DEEPLINK");
    }
}