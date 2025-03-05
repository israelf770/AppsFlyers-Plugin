package com.example.plugin;

//import com.android.tools.idea.logcat.LogcatService;
import com.intellij.openapi.project.Project;
import javax.swing.SwingUtilities;
import java.lang.reflect.Method;
import java.util.List;

public class LogcatProcessHandler {

    public static void startLogcat(Project project) {
        LogPopup.getDisplayedLogs().clear();
        if (LogPopup.getPopup() != null && LogPopup.getPopup().isVisible()) {
            LogPopup.getPopup().dispose();
            LogPopup.setPopup(null);
        }

        try {
            ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
                Class<?> logcatServiceClass = Class.forName("com.android.tools.idea.logcat.LogcatService", true, systemClassLoader);
            Method getInstanceMethod = logcatServiceClass.getMethod("getInstance");
            Object logcatService = getInstanceMethod.invoke(null);

            Class<?> logcatListenerClass = Class.forName("com.android.tools.idea.logcat.LogcatListener", true, systemClassLoader);
            Method addListenerMethod = logcatServiceClass.getMethod("addListener", Project.class, logcatListenerClass);
            addListenerMethod.invoke(logcatService, project, new LogcatListener());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class LogcatListener {
        public void onLogcatMessagesReceived(List<?> messages) {
            try {

                for (Object message : messages) {
                    Method getMessageMethod = message.getClass().getMethod("getMessage");
                    String logText = (String) getMessageMethod.invoke(message);
                    System.out.println(message);
                    if (logText.length() <= 14) continue; // Ignore short lines
                    String date = logText.substring(0, 14);

                    if (logText.contains("CONVERSION-")) {
                        processLog("CONVERSION", logText, date);
                    } else if (logText.contains("LAUNCH-")) {
                        processLog("LAUNCH", logText, date);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void processLog(String type, String text, String date) {

        if ("LAUNCH".equals(type) && text.contains("new task added: LAUNCH")) {
            SwingUtilities.invokeLater(() -> LogPopup.showPopup("new task added: LAUNCH"));
            return;
        }

        String formattedLog = LogUtils.extractKeyValueFromLog(text);
        if (text.contains("result:")) {
            int resIndex = text.indexOf("result");
            SwingUtilities.invokeLater(() -> LogPopup.showPopup(date + " / " + type + ": " + text.substring(resIndex)));
        }else if (formattedLog != null && !formattedLog.isEmpty()) {
            SwingUtilities.invokeLater(() -> LogPopup.showPopup(date + " / " + type + " " + formattedLog));
        }
    }

}
