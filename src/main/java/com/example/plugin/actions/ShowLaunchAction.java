
package com.example.plugin.actions;

import com.example.plugin.showLogs;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;
import javax.swing.Icon;
import com.intellij.openapi.util.IconLoader;




public class ShowLaunchAction extends AnAction {
    public static class MyClass {
        public static final Icon MY_ICON = IconLoader.getIcon("/icons/launchIcon.svg", MyClass.class);
    }
    public ShowLaunchAction() {
        super("LAUNCH", "Show launch logs", MyClass.MY_ICON);        // טקסט הכפתור
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        // הקריאה לפונקציה שסיננת קודם:
        showLogs.filterLogs("LAUNCH");
    }
}
