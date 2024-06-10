package com.uangel.rmq.handler;

import com.uangel.command.CommandInfo;
import com.uangel.model.SessionInfo;
import com.uangel.model.SessionManager;
import com.uangel.scenario.Scenario;
import com.uangel.scenario.handler.phases.ProcRecvPhase;
import com.uangel.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author dajin kim
 */
@Slf4j
public class RmqJsonConsumer {
    private final Scenario scenario;
    private final CommandInfo config;
    private final SessionManager sessionManager;

    public RmqJsonConsumer(Scenario scenario) {
        this.scenario = scenario;
        this.config = scenario.getCmdInfo();
        this.sessionManager = scenario.getSessionManager();
    }

    public void jsonMsgProcessing(byte[] msg) {
        if (scenario.isTestEnded()) return;

        try {
            // Byte Array -> Json String
            String json = new String(msg, StandardCharsets.UTF_8);

            // Parse Keyword Field
            Map<String, String> fields =  JsonUtil.getAllJsonFields(json);
            String sessionId = fields.get(config.getFieldKeyword());

            // log
            {
                String jsonUpper = json.toUpperCase();
                if (!jsonUpper.contains("HB") && !jsonUpper.contains("HEARTBEAT"))
                    log.debug("RmqJsonConsumer RecvMsg [{}]", JsonUtil.buildPretty(json));
            }

            if (sessionManager == null) {
                log.warn("RmqJsonConsumer Fail - SessionManager is Null");
                return;
            }

            if (fields.isEmpty()) {
                log.warn("RmqJsonConsumer Fail - Msg Fields Map is Empty");
                return;
            }

            // sessionId로 SessionManager 에 등록된 SessionInfo 조회
            SessionInfo sessionInfo = sessionManager.getSessionInfo(sessionId);
            if (sessionInfo != null) {
                ProcRecvPhase recvPhase = sessionInfo.getProcRecvPhase();
                recvPhase.handleMessage(json, sessionId, fields);
            } else if (!scenario.isOutScenario()) {
                // SessionInfo 없는 경우 세션 생성 후 시나리오 첫번째 부터 시작
                // SessionManager createSessionInfo 호출 -> sessionId 인자값 전달
                SessionInfo newSessionInfo = sessionManager.createSessionInfo(sessionId);

                if (newSessionInfo != null) {
                    if (!newSessionInfo.getProcRecvPhase().handleMessage(json, sessionId, fields))
                        log.warn("[{}] [{}] JsonConsumer Fail - new session recvPhase.handleMessage fail [MSG:{}]", scenario.getName(), sessionId, json);
                } else {
                    log.warn("[{}] [{}] JsonConsumer Fail - new session created fail [MSG:{}]", scenario.getName(), sessionId, json);
                }
            }

        } catch (Exception e) {
            log.error("RmqJsonConsumer.jsonMsgProcessing.Exception ", e);
        }

    }
}
