package com.uangel.rmq.handler;

import com.uangel.command.CommandInfo;
import com.uangel.model.SessionInfo;
import com.uangel.model.SessionManager;
import com.uangel.scenario.Scenario;
import com.uangel.scenario.handler.phases.ProcRecvPhase;
import com.uangel.scenario.phases.RecvPhase;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class IncomingHandler {
    private final Scenario scenario;
    private final CommandInfo config;
    private final SessionManager sessionManager;

    public IncomingHandler(Scenario scenario) {
        this.scenario = scenario;
        this.config = scenario.getCmdInfo();
        this.sessionManager = scenario.getSessionManager();
    }

    public void handle(String json, Map<String, String> fields) {
        String jsonUpper = json.toUpperCase();
        if (!jsonUpper.contains("HB") && !jsonUpper.contains("HEARTBEAT"))
            log.debug("RcvMsg [{}]", json);

        if (sessionManager == null || fields.isEmpty()) {
            log.warn("IncomingHandler Fail - Check SessionManager or Fields Map");
            return;
        }

        String sessionId = fields.get(config.getFieldKeyword());
        if (sessionId == null || sessionId.isEmpty()) {
            return;
        }

        // sessionId로 SessionManager 에 등록된 SessionInfo 조회
        SessionInfo sessionInfo = sessionManager.getSessionInfo(sessionId);
        if (sessionInfo != null) {
            ProcRecvPhase recvPhase = sessionInfo.getProcRecvPhase();
            recvPhase.handleMessage(json, sessionId, fields);
        } else if (!scenario.isOutScenario()) {
            if (!scenario.checkFirstMsg(json)) {
                log.warn("[{}] Fail to start new session - check msg type", sessionId);
                return;
            }

            String msgName = scenario.getFirstRcvPhaseName();
            log.info("Start new session (msgName:{}, id:{})", msgName, sessionId);

            // SessionInfo 없는 경우 세션 생성 후 시나리오 첫번째 부터 시작
            // SessionManager createSessionInfo 호출 -> sessionId 인자값 전달
            SessionInfo newSessionInfo = sessionManager.createSessionInfo(sessionId);
            if (newSessionInfo != null) {
                // 첫번째 RecvPhase 인덱스로 설정
                newSessionInfo.getCurrentIdx().set(scenario.getFirstRcvIdx());
                if (!newSessionInfo.getProcRecvPhase().handleMessage(json, sessionId, fields))
                    log.warn("[{}] Fail to handle msg - recvPhase.handleMessage fail [MSG:{}]", sessionId, json);
            } else {
                log.warn("[{}] Fail to handle msg - new session created fail [MSG:{}]", sessionId, json);
            }
        } else {
            log.debug("Skip Message - Outbound scenario (id:{})", sessionId);
        }
    }
}
