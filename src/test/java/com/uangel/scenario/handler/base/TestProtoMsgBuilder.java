package com.uangel.scenario.handler.base;

import com.uangel.command.CommandInfo;
import com.uangel.model.SessionInfo;
import com.uangel.reflection.JarReflection;
import com.uangel.scenario.Scenario;
import com.uangel.scenario.ScenarioBuilder;
import com.uangel.scenario.phases.SendPhase;
import com.uangel.service.AppInstance;
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

    private final AppInstance instance = AppInstance.getInstance();
    private final KeywordMapper keywordMapper = KeywordMapper.getInstance();
    private final SessionInfo sessionInfo = new SessionInfo(0);

    @Before
    public void prepareUserCmd() throws ParseException {
        // KeywordMapper
        keywordMapper.addUserCmd("tId", "java.util.UUID.randomUUID().toString()");
        keywordMapper.addUserCmd("timestamp", "java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern(\"yyyy-MM-dd HH:mm:ss.SSS\"))");
        keywordMapper.addUserCmd("timestamp", "java.lang.System.currentTimeMillis()");

        sessionInfo.addField("tId" ,"TEST_TID");

        // Jar
        String jarPath = "./src/main/resources/proto/mrfp-external-msg-1.0.3.jar";
        JarReflection jarReflection = new JarReflection(jarPath);
        jarReflection.loadJarFile();
        instance.setJarReflection(jarReflection);

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
        instance.setCmdInfo(cmdInfo);
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
