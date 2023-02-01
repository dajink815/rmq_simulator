package com.uangel.scenario.handler.base;

import com.uangel.command.CommandInfo;
import com.uangel.model.SessionInfo;
import com.uangel.reflection.JarReflection;
import com.uangel.scenario.Scenario;
import com.uangel.scenario.ScenarioBuilder;
import com.uangel.scenario.phases.LoopPhase;
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
public class TestProtoMsgBuilder {

    private static final String SCENARIO_NAME = "mrfc_basic_hb.xml";
    private Scenario scenario;
    private SessionInfo sessionInfo;

    @Before
    public void prepareUserCmd() throws ParseException, IOException, SAXException {
        // CommandInfo
        String localIp = "127.0.0.1";
        String[] args = {"-sf", "./src/main/resources/scenario/" + SCENARIO_NAME,
                "-t", "proto", "-pf", "./src/main/resources/proto/mrfp-external-msg-1.0.3.jar",
                "-pkg", "com.uangel.protobuf.mrfp.external",
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
        keywordMapper.addUserCmd("timestamp", "java.lang.System.currentTimeMillis()");
        scenario.setKeywordMapper(keywordMapper);

        // Jar
        String jarPath = cmdInfo.getProtoFile();
        JarReflection jarReflection = new JarReflection(jarPath);
        jarReflection.loadJarFile();
        scenario.setJarReflection(jarReflection);
    }

    @Test
    public void testProtoMsgBuilder() {
        SendPhase sendPhase = (SendPhase) scenario.getPhase(0);
        ProtoMsgBuilder msgBuilder = new ProtoMsgBuilder(sessionInfo);
        byte[] msg = msgBuilder.build(sendPhase);
        System.out.println("Msg : " + msg);
    }

    @Test
    public void testLoopProtoMsgBuilder() {
        LoopPhase loopPhase = scenario.getLoopPhase(0);
        if (loopPhase == null) return;

        LoopProtoMsgBuilder msgBuilder = new LoopProtoMsgBuilder(scenario);
        byte[] msg = msgBuilder.build(loopPhase);
        System.out.println("Msg : " + msg);
    }
}
