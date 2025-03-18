package com.example.plugin.actions;

import com.example.plugin.LogcatProcessHandler;
import com.example.plugin.UI.LogToolWindowFactory;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;
import javax.swing.Icon;
import com.intellij.openapi.util.IconLoader;
import static com.example.plugin.UI.LogToolWindowFactory.deviceCombo;


public class RunAction extends AnAction {
    public static class MyClass {
        public static final Icon MY_ICON = IconLoader.getIcon("AllIcons.Actions.Execute", MyClass.class);
    }
    public RunAction() {
        super("RUN", "Run action", MyClass.MY_ICON);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        int selectedIndex = deviceCombo.getSelectedIndex();
        LogToolWindowFactory.loadDevices();
        deviceCombo.setSelectedIndex(selectedIndex);
        LogcatProcessHandler.startLogcat();
    }
}
