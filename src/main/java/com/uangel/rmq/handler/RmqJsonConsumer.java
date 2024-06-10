package com.uangel.rmq.handler;

import com.uangel.command.CommandInfo;
import com.uangel.scenario.Scenario;
import com.uangel.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * @author dajin kim
 */
@Slf4j
public class RmqJsonConsumer {
    private final Scenario scenario;
    private final CommandInfo config;
    private final IncomingHandler handler;

    public RmqJsonConsumer(Scenario scenario) {
        this.scenario = scenario;
        this.config = scenario.getCmdInfo();
        this.handler = new IncomingHandler(scenario);
    }

    public void jsonMsgProcessing(byte[] msg) {
        if (scenario.isTestEnded()) return;

        try {
            // Byte Array -> Json String
            String json = new String(msg, StandardCharsets.UTF_8);

            // Parse Keyword Field
            Map<String, String> fields = JsonUtil.getAllJsonFields(json);
            String sessionId = fields.get(config.getFieldKeyword());

            // log
            {
                String jsonUpper = json.toUpperCase();
                if (!jsonUpper.contains("HB") && !jsonUpper.contains("HEARTBEAT"))
                    log.debug("RmqJsonConsumer RecvMsg [{}]", JsonUtil.buildPretty(json));
            }

            handler.handle(sessionId, json, fields);
        } catch (Exception e) {
            log.error("RmqJsonConsumer.jsonMsgProcessing.Exception ", e);
        }

    }
}
