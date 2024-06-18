package com.uangel.rmq.handler;

import com.uangel.scenario.Scenario;
import com.uangel.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
public class RmqJsonConsumer {
    private final Scenario scenario;
    private final IncomingHandler handler;

    public RmqJsonConsumer(Scenario scenario) {
        this.scenario = scenario;
        this.handler = new IncomingHandler(scenario);
    }

    public void jsonMsgProcessing(byte[] msg) {
        if (scenario.isTestEnded()) return;

        try {
            // Byte Array -> Json String
            String json = new String(msg, StandardCharsets.UTF_8);
            if (json.isEmpty()) {
                log.warn("RmqJsonConsumer Fail - MsgJson is empty");
                return;
            }

            // Json String -> Pretty Json
            json = JsonUtil.buildPretty(json);

            // Parse Keyword Field
            Map<String, String> fields = JsonUtil.getAllJsonFields(json);

            handler.handle(json, fields);

        } catch (Exception e) {
            log.error("RmqJsonConsumer.jsonMsgProcessing.Exception ", e);
        }

    }
}
