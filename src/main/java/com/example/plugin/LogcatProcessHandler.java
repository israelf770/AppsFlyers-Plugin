package com.example.plugin;

import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Key;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;

public class LogcatProcessHandler {
    private static final Logger logger = Logger.getInstance(LogcatProcessHandler.class);

    public static void startLogcat() {
        try {
            logger.info("logcat listener started");
            // כאן אנחנו מריצים את הפקודה adb logcat
            ProcessBuilder builder = new ProcessBuilder("adb", "logcat", "*:V");
            // חשוב לוודא ש-ADB מותקן ונגיש (ולוודא שהנתיב שלו נמצא ב-PATH)
            Process process = builder.start();

            // יוצר ProcessHandler שמנהל את התהליך ומאפשר לנו לקבל פלט מהתהליך
            OSProcessHandler processHandler = getOsProcessHandler(process);

            // מתחילים לקבל התראות מהתהליך
            processHandler.startNotify();
        } catch (Exception e) {
            logger.error("Error starting adb logcat", e);
        }
    }

    private static @NotNull OSProcessHandler getOsProcessHandler(Process process) {
        OSProcessHandler processHandler = new OSProcessHandler(process, "adb logcat", StandardCharsets.UTF_8);

        // מוסיפים מאזין לתהליך כדי לקרוא את הפלט שמגיע
        processHandler.addProcessListener(new ProcessAdapter() {
            @Override
            public void onTextAvailable(@NotNull ProcessEvent event, @NotNull Key outputType) {
                String text = event.getText();
                // כאן נבדוק אם הפלט מכיל את התבנית שאנחנו מחפשים
                if (text.contains("InetDiag")) {
                    // אם נמצאה התבנית, נדפיס את ההודעה לקונסול
                    logger.info("Pattern Found: " + text);
                }
            }
        });
        return processHandler;
    }
}
