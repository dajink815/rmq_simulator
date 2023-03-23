package com.uangel.scenario.handler.phases;

import com.uangel.model.SessionInfo;
import com.uangel.scenario.phases.MsgPhase;
import com.uangel.scenario.phases.RecvPhase;
import com.uangel.util.StringUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author dajin kim
 */
@Slf4j
public class ProcRecvPhase extends ProcMsgPhase {

    // 프로세스 다 같이 공유? static
    // 시나리오 파싱 후 바로 세팅?
    private final Set<RecvPhase> optionalPhases = ConcurrentHashMap.newKeySet();

    public ProcRecvPhase(SessionInfo sessionInfo) {
        super(sessionInfo);
    }

    @Override
    public void run(MsgPhase msgPhase) {
        RecvPhase recvPhase = (RecvPhase) msgPhase;

        // Optional
        if (Boolean.TRUE.equals(recvPhase.getOptional())) {
            // Optional Phase 저장 후 따로 처리
            optionalPhases.add(recvPhase);
            log.debug("({}) Pass Optional [{}]", sessionInfo.getSessionId(), recvPhase.getMsgName());
            sessionInfo.execPhase(sessionInfo.increaseCurIdx());
        }

        // timeout

        // Recv 단계엔 메시지 수신 했을 때 다음 단계로 진행 가능
    }

    // 수신한 메시지 처리
    public boolean handleMessage(String json, String sessionId, Map<String, String> fields) {
        // 테스트 끝난 세션 skip
        if (this.sessionInfo.isSessionEnded() || !sessionManager.checkIndex(sessionInfo)) return false;

        MsgPhase curPhase = scenario.getPhase(sessionInfo.getCurIdx());
        if (StringUtil.notNull(sessionId))
            log.debug("({}) HandleMessage CurIndex ({})", sessionId, sessionInfo.getCurIdx());

        // Optional 처리 - Optional 메시지는 CurrentPhase 타입과 무관하게 처리 가능해야함
        RecvPhase optionalRecv = checkOptionalPhases(json);
        if (optionalRecv != null) {
            log.debug("CheckOptionalPhases True : {}", optionalRecv.getMsgName());

            // recv 태그의 next 속성 값 있을 경우 next label 처리
            ProcLabelPhase procLabelPhase = new ProcLabelPhase(scenario);
            scenario.addFields(fields);
            procLabelPhase.run(optionalRecv.getNext(), fields);
            return true;
        }

        // Optional 처리 후 RecvPhase 타입 체크
        // 현재 단계가 Recv 아니라면 skip
        if (!(curPhase instanceof RecvPhase)) return false;

        if (StringUtil.isNull(sessionId)) {
            // sessionId Null
            return true;
        }

        // check SessionId
        if (!sessionId.equals(sessionInfo.getSessionId())) {
            // SessionId가 동일하지 않지만 조건 맞는 경우 SessionId 갱신        // todo 그 외 조건?
            // 조건 1. RmqGen Receiver, RecvPhase 로 시작
            //       (처리 중인 Index == optional 아닌 첫번째 RecvPhase Index)
            if (sessionInfo.getCurIdx() == scenario.getFirstRecvPhaseIdx()) {
                sessionManager.changeSessionId(sessionInfo.getSessionId(), sessionId);
            } else {
                // UnExpected
                log.warn("({}) ProcRecvPhase MisMatch SessionInfo [{}]", sessionId, sessionInfo.getSessionId());
                return false;
            }
        }

        sessionInfo.addFields(fields);
        sessionInfo.execPhase(sessionInfo.increaseCurIdx());

        return true;
    }

    private RecvPhase checkOptionalPhases(String json) {

        RecvPhase recvPhase = optionalPhases.stream()
                .filter(r -> checkMsgName(r, json))
                .findFirst().orElse(null);

/*        if (recvPhase == null) {
            log.debug("checkOptionalPhases Result Null [\r\n{}]", json);
        } else {
            log.debug("checkOptionalPhases Result : {}", recvPhase.getMsgName());
        }*/

        return recvPhase;
    }

    private boolean checkMsgName(RecvPhase recvPhase, String json) {
        String msgName = recvPhase.getMsgName().toLowerCase();
        return json.toLowerCase().contains(msgName);
    }
}
