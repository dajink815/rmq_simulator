package com.uangel.rmq.handler;

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
    private final SessionManager sessionManager;

    public IncomingHandler(Scenario scenario) {
        this.scenario = scenario;
        this.sessionManager = scenario.getSessionManager();
    }

    public void handle(String sessionId, String json, Map<String, String> fields) {
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
            if (sessionId == null || sessionId.isEmpty()) return;

            int firstRcvIdx = scenario.getFirstRecvPhaseIdx();
            RecvPhase firstRcvPhase = (RecvPhase) scenario.getPhase(firstRcvIdx);
            String msgName = firstRcvPhase.getMsgName();

            if (!json.contains(msgName.toLowerCase()) && !json.contains(msgName.toUpperCase())) {
                log.warn("[{}] Fail to Create New Session - check msg type [MSG:{}]", sessionId, json);
                return;
            }

            // SessionInfo 없는 경우 세션 생성 후 시나리오 첫번째 부터 시작
            // SessionManager createSessionInfo 호출 -> sessionId 인자값 전달
            SessionInfo newSessionInfo = sessionManager.createSessionInfo(sessionId);

            if (newSessionInfo != null) {
                newSessionInfo.getCurrentIdx().set(firstRcvIdx);
                if (!newSessionInfo.getProcRecvPhase().handleMessage(json, sessionId, fields))
                    log.warn("[{}] Fail to handle msg - recvPhase.handleMessage fail [MSG:{}]", sessionId, json);
            } else {
                log.warn("[{}] Fail to handle msg - new session created fail [MSG:{}]", sessionId, json);
            }
        }
/*        // SessionInfo 조회 실패시 ProcRecvPhase 조회
        else {
            List<ProcRecvPhase> procRecvPhaseList = sessionManager.getRecvPhaseList();
            if (procRecvPhaseList.isEmpty()) return;

            scenario.getExecutorService().submit(() -> {
                for (ProcRecvPhase procRecvPhase : procRecvPhaseList) {
                    if (procRecvPhase.handleMessage(json, sessionId, fields)) {
                        break;
                    }
                }
            });
        }*/
    }
}
