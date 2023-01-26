package com.uangel.scenario.handler.base;

import com.uangel.command.CommandInfo;
import com.uangel.model.SessionInfo;
import com.uangel.reflection.JarReflection;
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
public class TestProtoMsgBuilder {

    // todo
    private final KeywordMapper keywordMapper = new KeywordMapper();
    private SessionInfo sessionInfo;

    @Before
    public void prepareUserCmd() throws ParseException, IOException, SAXException {
        // KeywordMapper
        keywordMapper.addUserCmd("tId", "java.util.UUID.randomUUID().toString()");
        keywordMapper.addUserCmd("timestamp", "java.lang.System.currentTimeMillis()");

        String filePath = "./src/main/resources/scenario/mrfc_basic.xml";
        Scenario scenario = ScenarioBuilder.fromXMLFileName(filePath);
        sessionInfo = new SessionInfo(0, scenario);
        sessionInfo.addField("tId" ,"TEST_TID");

        // Jar
        String jarPath = "./src/main/resources/proto/mrfp-external-msg-1.0.3.jar";
        JarReflection jarReflection = new JarReflection(jarPath);
        jarReflection.loadJarFile();

        // CommandInfo
        String localIp = "127.0.0.1";
        String[] args = {"-sf", "./src/main/resources/scenario/mrfc_basic.xml",
                "-t", "proto", "-pf", "./src/main/resources/proto/mrfp-external-msg-1.0.3.jar",
                "-pkg", "com.uangel.protobuf.mrfp.external",
                "-rl", "local_queue", "-rh", localIp,
                "-rp", "5672", "-m", "1"};

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(CommandInfo.createOptions(), args);
        CommandInfo cmdInfo = new CommandInfo(cmd);
    }

    @Test
    public void protoMsgBuild() throws IOException, SAXException{

        String filePath = "./src/main/resources/scenario/mrfc_basic.xml";
        Scenario scenario = ScenarioBuilder.fromXMLFileName(filePath);

        SendPhase sendPhase = (SendPhase) scenario.getPhase(0);
        ProtoMsgBuilder msgBuilder = new ProtoMsgBuilder(sessionInfo);
        byte[] msg = msgBuilder.build(sendPhase);
        System.out.println("Msg : " + msg);
    }
}
