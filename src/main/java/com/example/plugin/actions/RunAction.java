package com.example.plugin.actions;

import com.example.plugin.LogcatProcessHandler;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;
import javax.swing.Icon;
import com.intellij.openapi.util.IconLoader;


public class RunAction extends AnAction {
    public static class MyClass {
        public static final Icon MY_ICON = IconLoader.getIcon("/icons/runIcon2.svg", MyClass.class);
    }
    public RunAction() {
        super("RUN", "Run action", MyClass.MY_ICON);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        LogcatProcessHandler.resetSelectedDevice();
        LogcatProcessHandler.startLogcat();
    }
}
