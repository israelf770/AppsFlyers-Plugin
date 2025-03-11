package com.example.plugin;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

public class LogUtils {

    // method to extract needed info from logs
    public static String extractMessageFromJson(String type, String logText) {
        try {
            int jsonStartIndex = logText.indexOf("{");
            int jsonEndIndex = logText.length()-1;

            // Check if indices are valid
            if (jsonStartIndex == -1 || jsonEndIndex == -1 || jsonEndIndex < jsonStartIndex) {
                System.err.println("Error: JSON not found in log text.");
                return null;
            }

            // Check if indices are within string bounds
            if (jsonEndIndex + 1 > logText.length()) {
                System.err.println("Error: Invalid substring range.");
                return null;
            }

            String jsonPart = logText.substring(jsonStartIndex, jsonEndIndex + 1).trim();

            if(type.equals("CONVERSION")||type.equals("LAUNCH")){
            JsonObject jsonObject = JsonParser.parseString(jsonPart).getAsJsonObject();
            return  jsonObject.has("uid") ? "UID: " + jsonObject.get("uid").getAsString() : "UID Not Found";
            } else if (type.equals("EVENT")) {
                JsonObject jsonObject = JsonParser.parseString(jsonPart).getAsJsonObject();
                String eventName = jsonObject.has("eventName") ?  jsonObject.get("eventName").getAsString() : "Event Name Not Found";
                String eventData = jsonObject.has("eventValue") ?  jsonObject.get("eventValue").getAsString() : "Event Value Not Found";
                return "\n{"+ "\n" +" \"eventName\":"+'\"'+eventName+'\"' +"," + "\n" + " \"eventValue\":"+'\"'+eventData +'\"'+ "\n" + "}";
            }
            return null;

        } catch (JsonSyntaxException e) {
            System.err.println("JSON parsing error: " + e.getMessage());
            return null;
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            return null;
        }
    }

}