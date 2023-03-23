package com.uangel.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ValueNode;
import org.json.JSONObject;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author dajin kim
 */
public class TestJsonUtil {

    private String dialogStartReqMsg = "{\n" +
            "  \"header\": {\n" +
            "    \"msgFrom\": \"MRFC\",\n" +
            "    \"type\": \"DIALOG_START_REQ\",\n" +
            "    \"dialogId\": \"420cd67a-40f4-4f87-84b2-af0a53efb028_1\",\n" +
            "    \"tId\": \"32cc8969-0915-4d32-b95b-303b7e21f0b5\",\n" +
            "    \"timestamp\": \"2023-01-31 08:32:03.796\"\n" +
            "  },\n" +
            "  \"DialogStartReq\": {\n" +
            "    \"fromNo\": \"010-1111-2222\",\n" +
            "    \"toNo\": \"010-3333-4444\",\n" +
            "    \"sdp\": \"v\\\\u003d0\\\\r\\\\no\\\\u003damf 0 0 IN IP4 100.100.100.57\\\\r\\\\ns\\\\u003d-\\\\r\\\\nc\\\\u003dIN IP4 192.168.7.34\\\\r\\\\nt\\\\u003d0 0\\\\r\\\\nm\\\\u003daudio 10022 RTP/AVP 97 99\\\\r\\\\na\\\\u003drtpmap:97 AMR-WB/16000/1\\\\r\\\\na\\\\u003dfmtp:97 octet-align\\\\u003d1; mode-set\\\\u003d7\\\\r\\\\na\\\\u003drtpmap:99 telephone-event/8000\\\\r\\\\na\\\\u003dfmtp:99 0-16\\\\r\\\\na\\\\u003dptime:20\\\\r\\\\na\\\\u003dsendrecv\\\\r\\\\na\\\\u003ddirection:active\\\\r\\\\n\"\n" +
            "  }\n" +
            "}";

    private String hbReqMsg = "{\n" +
            "  \"header\": {\n" +
            "    \"type\": \"heartbeat_req\",\n" +
            "    \"msgFrom\": \"C_AMF_0\",\n" +
            "    \"transactionId\": \"c39d7b86-c609-4b70-ad9e-b67b33a261ba\",\n" +
            "    \"reasonCode\": 0\n" +
            "  },\n" +
            "  \"body\": {\n" +
            "    \"amfId\": 0,\n" +
            "    \"cpuUsage\": 3,\n" +
            "    \"memoryUsage\": 82,\n" +
            "    \"sessionCount\": 0\n" +
            "  }\n" +
            "}";

    private void parseJsonStr(String json) {

        // Json 포맷 맞는지 먼저 체크
        Pattern keyPattern = Pattern.compile("\\{(.*?)\\}");
        Matcher m = keyPattern.matcher(json);

        while (m.find()) {
            System.out.println("group : " + m.group());
            System.out.println("group 0 : " + m.group(0));
        }

        if (json.startsWith("{")) {
            json = json.substring(1);
        }
        if (json.endsWith("}")) {
            json = json.substring(0, json.length() - 1);
        }
        System.out.println(json);
    }

    @Test
    public void parseJsonUnKnownField() {
        System.out.println(dialogStartReqMsg);
        parseJsonStr(dialogStartReqMsg);

        System.out.println(hbReqMsg);
        parseJsonStr(hbReqMsg);

        JSONObject reader = new JSONObject(hbReqMsg);
        Iterator iteratorObj = reader.keys();
        ArrayList<String> allKeys=new ArrayList<>();

        while (iteratorObj.hasNext()) {
            String getJsonObj = (String) iteratorObj.next();
            System.out.println("KEY: " + "------>" + getJsonObj);
            allKeys.add(getJsonObj);
        }
        System.out.println(allKeys);
    }

    @Test
    public void parseJsonKnownField() {
        String json = "{\n" +
                "  \"toUser\": \"010-1111-2222\",\n" +
                "  \"fromUser\": \"010-3333-4444\"\n" +
                "}";

        System.out.println(json);

        JSONObject jObject = new JSONObject(json);

        String toUser = jObject.getString("toUser");
        String fromUser = jObject.getString("fromUser");

        System.out.println("toUser : " + toUser);
        System.out.println("fromUser : " + fromUser);
    }

    @Test
    public void jsonParsing1() {
        String jsonString = "{\"title\": \"how to get stroage size\","
                + "\"url\": \"https://codechacha.com/ko/get-free-and-total-size-of-volumes-in-android/\","
                + "\"draft\": false,"
                + "\"star\": 10"
                + "}";

        // JSONObjet를 가져와서 key-value를 읽습니다.
        JSONObject jObject = new JSONObject(jsonString);
        String title = jObject.getString("title");
        String url = jObject.getString("url");
        Boolean draft = jObject.getBoolean("draft");
        int star = jObject.getInt("star");

        System.out.println("title: " + title);
        System.out.println("url: " + url);
        System.out.println("draft: " + draft);
        System.out.println("star: " + star);
    }

    @Test
    public void jsonParsing2() {
        String jsonString =
                "{"
                        +   "\"post1\": {"
                        +       "\"title\": \"how to get stroage size\","
                        +       "\"url\": \"https://codechacha.com/ko/get-free-and-total-size-of-volumes-in-android/\","
                        +       "\"draft\": false"
                        +"  },"
                        +   "\"post2\": {"
                        +       "\"title\": \"Android Q, Scoped Storage\","
                        +       "\"url\": \"https://codechacha.com/ko/android-q-scoped-storage/\","
                        +       "\"draft\": false"
                        +   "}"
                        +"}";

        // 가장 큰 JSONObject를 가져옵니다.
        JSONObject jObject = new JSONObject(jsonString);

        // 첫번째 JSONObject를 가져와서 key-value를 읽습니다.
        JSONObject post1Object = jObject.getJSONObject("post1");
        System.out.println(post1Object.toString());
        System.out.println();
        String title = post1Object.getString("title");
        String url = post1Object.getString("url");
        boolean draft = post1Object.getBoolean("draft");
        System.out.println("title(post1): " + title);
        System.out.println("url(post1): " + url);
        System.out.println("draft(post1): " + draft);
        System.out.println();

        // 두번째 JSONObject를 가져와서 key-value를 읽습니다.
        JSONObject post2Object = jObject.getJSONObject("post2");
        System.out.println(post2Object.toString());
        System.out.println();
        title = post2Object.getString("title");
        url = post2Object.getString("url");
        draft = post2Object.getBoolean("draft");
        System.out.println("title(post1): " + title);
        System.out.println("url(post1): " + url);
        System.out.println("draft(post1): " + draft);
    }

    @Test
    public void testParsingUnknownJsonFormat() throws IOException {
        System.out.println(JsonUtil.buildPretty(dialogStartReqMsg));
        System.out.println(getAllJsonFields(dialogStartReqMsg));

        System.out.println("======= Parse File Json Msg =======");
        System.out.println(getAllFileFields("src/test/resources/json/data.json"));
    }

    private static Map<String, String> getAllFileFields(String filePath) throws IOException {
        // jackson objectMapper 객체 생성
        ObjectMapper mapper = new ObjectMapper();

        File file = new File(filePath);
        Map<String, String> map = new HashMap<>();

        if (!file.exists() || !file.isFile()) return map;

        // JsonNode 생성 (readTree, readValue)
        JsonNode jsonNode = mapper.readTree(file);
        addKeys2("", jsonNode, map);
        return map;
    }

    private static Map<String, String> getAllJsonFields(String json) throws JsonProcessingException {
        // jackson objectMapper 객체 생성
        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> map = new HashMap<>();

        // JsonNode 생성 (readTree, readValue)
        JsonNode jsonNode = mapper.readTree(json);
        //jsonNode = mapper.readValue(json, JsonNode.class);

        addKeys2("", jsonNode, map);
        return map;
    }

    private static void addKeys(String currentPath, JsonNode jsonNode, Map<String, String> map) {
        if (jsonNode.isObject()) {
            ObjectNode objectNode = (ObjectNode) jsonNode;
            Iterator<Map.Entry<String, JsonNode>> iter = objectNode.fields();
            String pathPrefix = currentPath.isEmpty()? "" : currentPath + ".";

            while (iter.hasNext()) {
                Map.Entry<String, JsonNode> entry = iter.next();
                addKeys(pathPrefix + entry.getKey(), entry.getValue(), map);
            }
        } else if (jsonNode.isArray()) {
            ArrayNode arrayNode = (ArrayNode) jsonNode;
            for (int i = 0; i < arrayNode.size(); i++) {
                addKeys(currentPath + "[" + i + "]", arrayNode.get(i), map);
            }
        } else if (jsonNode.isValueNode()) {
            ValueNode valueNode = (ValueNode) jsonNode;
            map.put(currentPath, valueNode.asText());
        }
    }

    private static void addKeys2(String currentPath, JsonNode jsonNode, Map<String, String> map) {
        if (jsonNode.isObject()) {
            ObjectNode objectNode = (ObjectNode) jsonNode;
            Iterator<Map.Entry<String, JsonNode>> iter = objectNode.fields();

            while (iter.hasNext()) {
                Map.Entry<String, JsonNode> entry = iter.next();
                addKeys2(entry.getKey(), entry.getValue(), map);
            }
        } else if (jsonNode.isArray()) {
            ArrayNode arrayNode = (ArrayNode) jsonNode;
            for (int i = 0; i < arrayNode.size(); i++) {
                addKeys2(currentPath + "[" + i + "]", arrayNode.get(i), map);
            }
        } else if (jsonNode.isValueNode()) {
            ValueNode valueNode = (ValueNode) jsonNode;
            map.put(currentPath, valueNode.asText());
        }
    }

    @Test
    public void testGetAllFileFields() {
        String filePath = "src/main/resources/json/user_cmd.json";
        Map<String, String> userCmdFields = JsonUtil.getAllFileFields(filePath);
        System.out.println(userCmdFields);
    }
}
