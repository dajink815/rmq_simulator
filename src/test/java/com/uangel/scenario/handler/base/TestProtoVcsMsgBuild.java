package com.uangel.scenario.handler.base;

import com.uangel.command.CommandInfo;
import com.uangel.model.SessionInfo;
import com.uangel.reflection.JarReflection;
import com.uangel.scenario.Scenario;
import com.uangel.scenario.ScenarioBuilder;
import com.uangel.scenario.phases.LoopPhase;
import com.uangel.scenario.phases.SendPhase;
import com.uangel.scenario.type.OutMsgType;
//import com.uangel.vcs.*;
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
public class TestProtoVcsMsgBuild {
    private static final String SCENARIO_NAME = "amf_testvcif_basic.xml";
    private Scenario scenario;
    private SessionInfo sessionInfo;

    @Before
    public void prepareUserCmd() throws ParseException, IOException, SAXException {
        // CommandInfo
        String localIp = "127.0.0.1";
        String[] args = {"-sf", "./src/main/resources/scenario/" + SCENARIO_NAME,
                "-t", "proto", "-pf", "./src/main/resources/proto/vcs-proto-msg-1.0.2.jar",
                "-pkg", "com.uangel.vcs",
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

        // Jar
        String jarPath = cmdInfo.getProtoFile();
        JarReflection jarReflection = new JarReflection(jarPath);
        jarReflection.loadJarFile();
        scenario.setJarReflection(jarReflection);
    }

    @Test
    public void testProtoMsgBuilder() {
        SendPhase sendPhase = scenario.getFirstSendPhase();
        if (sendPhase == null) return;
        ProtoMsgBuilder msgBuilder = new ProtoMsgBuilder(sessionInfo, OutMsgType.SEND);
        byte[] msg = msgBuilder.build(sendPhase);
        System.out.println("Msg : " + msg);
    }

    @Test
    public void testLoopProtoMsgBuilder() {
        LoopPhase loopPhase = scenario.getLoopPhase(0);
        if (loopPhase == null) return;

        ProtoMsgBuilder msgBuilder = new ProtoMsgBuilder(scenario, OutMsgType.LOOP);
        byte[] msg = msgBuilder.build(loopPhase);
        System.out.println("Msg : " + msg);
    }

/*    @Test
    public void buildRecStartReq() {
        RecData caller = RecData.newBuilder()
                .setFile("recdataFile")
                .setSId("sId")
                .build();

        MediaData mediaData = MediaData.newBuilder()
                .setCodec(Codec.ALAW)
                .setPayloadId(100)
                .setOctetAligned(true)
                .build();

        RecStartReq recStartReq = RecStartReq.newBuilder()
                .setRecordId("recordId")
                .setTaskId("taskId")
                .setType(1)
                .setFilePath("filePath")
                .setCaller(caller)
                .setRtpInfo(mediaData)
                .build();

        Header header = Header.newBuilder()
                .setType("REC_START_REQ")
                .setMsgFrom("amf")
                .setTransactionId("tId")
                .build();

        Message message = Message.newBuilder()
                .setHeader(header)
                .setRecStartReq(recStartReq)
                .build();

        System.out.println("Proto Msg :");
        System.out.println(message);

    }*/
}
