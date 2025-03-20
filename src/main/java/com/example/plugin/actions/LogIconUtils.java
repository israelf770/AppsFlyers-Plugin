package com.example.plugin.actions;

    import com.intellij.openapi.util.IconLoader;
    import javax.swing.Icon;

    public class LogIconUtils {

        public static Icon getLogIcon(String log) {
            if (log.contains("ERROR") || log.contains("FAILURE") || log.contains("No deep link")) {
                return IconLoader.getIcon("AllIcons.Actions.QuickfixBulb", LogIconUtils.class);
            }else if (log.contains("{") && log.contains("}")) {
                return IconLoader.getIcon("AllIcons.FileTypes.Json", LogIconUtils.class);
            }
            else {
                return IconLoader.getIcon("AllIcons.General.BalloonInformation", LogIconUtils.class);
            }
        }
    }