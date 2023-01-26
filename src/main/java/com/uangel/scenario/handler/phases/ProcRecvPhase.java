package com.uangel.scenario.handler.phases;

import com.uangel.model.SessionInfo;
import com.uangel.scenario.phases.MsgPhase;
import com.uangel.scenario.phases.RecvPhase;

import java.util.Map;

/**
 * @author dajin kim
 */
public class ProcRecvPhase extends ProcMsgPhase {

    public ProcRecvPhase(SessionInfo sessionInfo) {
        super(sessionInfo);
    }

    @Override
    public void run(MsgPhase msgPhase) {
        RecvPhase recvPhase = (RecvPhase) msgPhase;

        // Optional?
        // timeout

        // Recv 단계엔 메시지 수신 했을 때 다음 단계로 진행 가능
    }

    // 수신한 메시지 처리
    public boolean handleMessage(String json, String sessionId, Map<String, String> fields) {
        // 테스트 끝난 세션 skip
        if (this.sessionInfo.isSessionEnded() || !sessionManager.checkIndex(sessionInfo)) return false;

        MsgPhase curPhase = scenario.getPhase(sessionInfo.getCurIdx());
        // Optional 처리?
        // 현재 단계가 Recv 아니라면 skip
        if (!(curPhase instanceof RecvPhase)) return false;
        RecvPhase curRecvPhase = (RecvPhase) curPhase;

        // check SessionId
        if (!sessionId.equals(sessionInfo.getSessionId())) {
            // SessionId가 동일하지 않지만 조건 맞는 경우 SessionId 갱신
            // todo 그 외 조건?
            if (sessionInfo.getCurIdx() == 0) {
                //sessionInfo.setSessionId(sessionId);
                sessionManager.changeSessionId(sessionInfo.getSessionId(), sessionId);
            } else {
                // UnExpected
                return false;
            }
        }

        sessionInfo.addFields(fields);
        sessionInfo.execPhase(sessionInfo.increaseCurIdx());

        return true;
    }
}
