package com.appsFlyers.plugin.tools;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class TooltipLoader {
    private static Properties properties = new Properties();

    static {
        try (InputStream input = TooltipLoader.class.getResourceAsStream("../../resources/tooltips.properties")) {
            if (input == null) {
                System.out.println("Sorry, unable to find tooltips.properties");
            }
            properties.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static String getTooltip(String key) {
        return properties.getProperty(key);
    }
}