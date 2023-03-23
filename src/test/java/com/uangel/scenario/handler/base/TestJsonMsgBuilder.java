package com.uangel.scenario.handler.base;

import com.uangel.command.CommandInfo;
import com.uangel.model.SessionInfo;
import com.uangel.scenario.Scenario;
import com.uangel.scenario.ScenarioBuilder;
import com.uangel.scenario.phases.LoopPhase;
import com.uangel.scenario.phases.SendPhase;
import com.uangel.scenario.type.OutMsgType;
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

    private Scenario scenario;
    private SessionInfo sessionInfo;

    @Before
    public void prepareUserCmd() throws ParseException, IOException, SAXException {
        // CommandInfo
        String scenarioPath = "./src/main/resources/scenario/";
        String scenarioName = "mrfc_basic_json_hb.xml";
        scenarioName = "mrfc_basic_nop.xml";
        scenarioName = "amf_testvcif_basic.xml";

        String localIp = "127.0.0.1";
        String[] args = {"-sf", scenarioPath + scenarioName,
                "-t", "json",
                "-rl", "local_queue", "-rh", localIp,
                "-rp", "5672", "-m", "1"};

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(CommandInfo.createOptions(), args);
        CommandInfo cmdInfo = new CommandInfo(cmd);

        String filePath = cmdInfo.getScenarioFile();
        scenario = ScenarioBuilder.fromXMLFileName(filePath, false);

        scenario.setCmdInfo(cmdInfo);

        sessionInfo = new SessionInfo(0, scenario);
        sessionInfo.addField("tId" ,"TEST_TID");

        // KeywordMapper
        KeywordMapper keywordMapper = new KeywordMapper(scenario);
        keywordMapper.addUserCmd("tId", "java.util.UUID.randomUUID().toString()");
        keywordMapper.addUserCmd("timestamp", "java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern(\"yyyy-MM-dd HH:mm:ss.SSS\"))");

        scenario.setKeywordMapper(keywordMapper);
    }

    @Test
    public void jsonMsgBuild() {
        SendPhase sendPhase = scenario.getFirstSendPhase();
        JsonMsgBuilder msgBuilder = new JsonMsgBuilder(sessionInfo, OutMsgType.SEND);
        byte[] msg = msgBuilder.build(sendPhase);
        String strMsg = new String(msg);
        System.out.println("Msg : " + strMsg);
    }

    @Test
    public void loopJsonMsgBuild() {
        LoopPhase loopPhase = scenario.getLoopPhase(0);
        if (loopPhase == null) return;

        JsonMsgBuilder jsonMsgBuilder = new JsonMsgBuilder(scenario, OutMsgType.LOOP);
        byte[] msg = jsonMsgBuilder.build(loopPhase);
        System.out.println("Msg : " + msg);
    }

}
