package com.uangel.scenario.handler.base;

import com.uangel.command.CommandInfo;
import com.uangel.model.SessionInfo;
import com.uangel.scenario.Scenario;
import com.uangel.scenario.ScenarioBuilder;
import com.uangel.scenario.phases.SendPhase;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.ParseException;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author dajin kim
 */
public class TestJsonMsgBuilder {

    private Scenario scenario;
    private SessionInfo sessionInfo;

    @Before
    public void prepareUserCmd() throws ParseException, IOException, SAXException {
        // CommandInfo
        String localIp = "127.0.0.1";
        String[] args = {"-sf", "./src/main/resources/scenario/mrfc_basic_json.xml",
                "-t", "json",
                "-rl", "local_queue", "-rh", localIp,
                "-rp", "5672", "-m", "1"};

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(CommandInfo.createOptions(), args);
        CommandInfo cmdInfo = new CommandInfo(cmd);

        String filePath = cmdInfo.getScenarioFile();
        scenario = ScenarioBuilder.fromXMLFileName(filePath);

        scenario.setCmdInfo(cmdInfo);

        sessionInfo = new SessionInfo(0, scenario);
        sessionInfo.addField("tId" ,"TEST_TID");

        // KeywordMapper
        KeywordMapper keywordMapper = new KeywordMapper(scenario);
        keywordMapper.addUserCmd("tId", "java.util.UUID.randomUUID().toString()");
        //keywordMapper.addUserCmd("timestamp", "java.lang.System.currentTimeMillis()");
        keywordMapper.addUserCmd("timestamp", "java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern(\"yyyy-MM-dd HH:mm:ss.SSS\"))");

        scenario.setKeywordMapper(keywordMapper);
    }

    @Test
    public void jsonMsgBuild() {
        SendPhase sendPhase = (SendPhase) scenario.getPhase(0);
        JsonMsgBuilder msgBuilder = new JsonMsgBuilder(sessionInfo);
        byte[] msg = msgBuilder.build(sendPhase);
        String strMsg = new String(msg);
        System.out.println("Msg : " + strMsg);

    }

    private void parseJsonStr(String json) {

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
    public void parseJson() {
        String json = "{\n" +
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

        System.out.println(json);
        parseJsonStr(json);
    }
}
