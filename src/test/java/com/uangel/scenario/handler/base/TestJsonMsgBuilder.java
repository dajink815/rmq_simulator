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

/**
 * @author dajin kim
 */
public class TestJsonMsgBuilder {
    private final KeywordMapper keywordMapper = new KeywordMapper();
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
        System.out.println("Msg : " + new String(msg));
    }
}
