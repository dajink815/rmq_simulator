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
            log.debug("RmqJsonConsumer RecvMsg [{}]", JsonUtil.buildPretty(json));

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
            }
            // SessionInfo 조회 실패시 ProcRecvPhase 조회
            else {
                List<ProcRecvPhase> procRecvPhaseList = sessionManager.getRecvPhaseList();
                if (procRecvPhaseList.isEmpty()) return;

                scenario.getExecutorService().submit(() -> {
                    for (ProcRecvPhase procRecvPhase : procRecvPhaseList) {
                        if(procRecvPhase.handleMessage(json, sessionId, fields)){
                            break;
                        }
                    }
                });
            }

        } catch (Exception e) {
            log.error("RmqJsonConsumer.jsonMsgProcessing.Exception ", e);
        }

    }
}
