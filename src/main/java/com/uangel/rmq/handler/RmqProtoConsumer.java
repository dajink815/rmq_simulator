package com.uangel.rmq.handler;

import com.uangel.command.CommandInfo;
import com.uangel.reflection.JarReflection;
import com.uangel.reflection.ProtoUtil;
import com.uangel.scenario.Scenario;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class RmqProtoConsumer {
    private final Scenario scenario;
    private final CommandInfo config;
    private final JarReflection jarReflection;
    private final IncomingHandler handler;


    public RmqProtoConsumer(Scenario scenario) {
        this.scenario = scenario;
        this.config = scenario.getCmdInfo();
        this.jarReflection = scenario.getJarReflection();
        this.handler = new IncomingHandler(scenario);
    }

    public void protoMsgProcessing(byte[] msg) {
        if (scenario.isTestEnded()) return;

        try {
            // Byte Array -> Object
            String className = config.getProtoPkg() + scenario.getMsgClassName();
            Object msgObj = jarReflection.parseFrom(className, msg);
            if (msgObj == null) {
                log.warn("RmqProtoConsumer Fail - MsgObj is null [Class:{}]", className);
                return;
            }

            // Object -> Pretty Json
            String json = ProtoUtil.buildProto(msgObj);

            // Parse Keyword Field
            Map<String, String> fields = jarReflection.getAllFieldsMap(msgObj);

            handler.handle(json, fields);

        } catch (Exception e) {
            log.error("RmqProtoConsumer.protoMsgProcessing.Exception ", e);
        }

    }
}
