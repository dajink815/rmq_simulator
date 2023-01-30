package com.uangel.util;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;

/**
 * @author dajin kim
 */
public class JsonUtil {

    private JsonUtil() {
        // nothing
    }

    // Object -> Pretty JSON
    public static String buildPretty(Object obj) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(obj);
    }

    // JSON -> Pretty JSON
    public static String buildPretty(String json) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(JsonParser.parseString(json));
    }

}
