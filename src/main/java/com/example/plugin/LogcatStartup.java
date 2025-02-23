package com.example.plugin;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class LogcatStartup implements StartupActivity {
    @Override
    public void runActivity(@NotNull Project project) {
        // הצגת הודעה למשתמש על התחלת ההאזנה ל-logcat
        Notifications.Bus.notify(new Notification(
                "Logcat Listener",
                "Logcat listener started",
                "Logcat listener started",
                NotificationType.INFORMATION
        ));

        // הפעלת הלוגים אוטומטית
        LogcatProcessHandler.startLogcat();
    }
}
