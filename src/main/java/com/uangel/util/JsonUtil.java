package com.uangel.util;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ValueNode;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author dajin kim
 */
@Slf4j
public class JsonUtil {

    private JsonUtil() {
        // nothing
    }

    // JSON -> ClassType Object
    public static <T> T parse(String json, Type classType) {
        Gson gson = new Gson();
        return gson.fromJson(json, classType);
    }

    // JsonElement -> ClassType Object
    public static <T> T parse(JsonElement json, Class<T> classType) {
        Gson gson = new Gson();
        return gson.fromJson(json, classType);
    }

    // Object -> JSON
    public static String build(Object obj) {
        Gson gson = new Gson();
        return gson.toJson(obj);
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

    // String -> JsonElement
    public static JsonElement build(String json) {
        Gson gson = new Gson();
        return gson.toJsonTree(json);
    }

    // Object -> JsonElement
    public static JsonElement build(Object obj, Type objType) {
        Gson gson = new GsonBuilder().create();
        return gson.toJsonTree(obj, objType);
    }

    public static Map<String, String> getAllFileFields(String filePath) {
        Map<String, String> map = new HashMap<>();

        try {
            // jackson objectMapper 객체 생성
            ObjectMapper mapper = new ObjectMapper();

            File file = new File(filePath);
            if (!file.exists() || !file.isFile()) return map;

            // JsonNode 생성 (readTree, readValue)
            JsonNode jsonNode = mapper.readTree(file);
            getAllFieldsMap("", jsonNode, map);
        } catch (IOException e) {
            log.error("JsonUtil.getAllFileFields.Exception [{}]", filePath, e);
        }
        return map;
    }

    public static Map<String, String> getAllJsonFields(String json) {
        Map<String, String> map = new HashMap<>();

        try {
            // jackson objectMapper 객체 생성
            ObjectMapper mapper = new ObjectMapper();

            MyDto readValue = mapper.readValue(json, MyDto.class);
            log.debug("MyDto : [{}]", readValue);

            // JsonNode 생성 (readTree, readValue)
            JsonNode jsonNode = mapper.readTree(json);
            getAllFieldsMap("", jsonNode, map);
        } catch (JsonProcessingException e) {
            log.error("JsonUtil.getAllJsonFields.Exception [{}]", json, e);
        }
        return map;
    }

    public static void getAllFieldsMap(String currentPath, JsonNode jsonNode, Map<String, String> map) {
        if (jsonNode.isObject()) {
            ObjectNode objectNode = (ObjectNode) jsonNode;
            Iterator<Map.Entry<String, JsonNode>> iter = objectNode.fields();

            while (iter.hasNext()) {
                Map.Entry<String, JsonNode> entry = iter.next();
                getAllFieldsMap(entry.getKey(), entry.getValue(), map);
            }
        } else if (jsonNode.isArray()) {
            ArrayNode arrayNode = (ArrayNode) jsonNode;
            for (int i = 0; i < arrayNode.size(); i++) {
                getAllFieldsMap(currentPath + "[" + i + "]", arrayNode.get(i), map);
            }
        } else if (jsonNode.isValueNode()) {
            ValueNode valueNode = (ValueNode) jsonNode;
            map.put(currentPath, valueNode.asText());
        }
    }

    public class MyDto {

        private String stringValue;
        private int intValue;
        private boolean booleanValue;

        // standard constructor, getters and setters


        @Override
        public String toString() {
            return "MyDto{" +
                    "stringValue='" + stringValue + '\'' +
                    ", intValue=" + intValue +
                    ", booleanValue=" + booleanValue +
                    '}';
        }
    }
}
