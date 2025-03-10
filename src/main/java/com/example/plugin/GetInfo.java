package com.example.plugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class GetInfo {
    public static List<String> getConnectedDevices(String adbPath) throws IOException {
        List<String> devices = new ArrayList<>();
        ProcessBuilder builder = new ProcessBuilder(adbPath, "devices");
        Process process = builder.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            reader.readLine();

            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    String[] parts = line.split("\\s+");
                    if (parts.length >= 2) {
                        devices.add(parts[0]);
                    }
                }
            }
        }

        return devices;
    }

    public static String getAdbPath() {
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            return System.getProperty("user.home") + "\\AppData\\Local\\Android\\Sdk\\platform-tools\\adb.exe";
        } else if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            return "/Users/" + System.getProperty("user.name") + "/Library/Android/sdk/platform-tools/adb";
        } else {
            throw new RuntimeException("Unsupported OS");
        }
    }
}
