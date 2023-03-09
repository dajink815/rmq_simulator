package com.uangel.scenario.handler.base;

import com.uangel.model.SessionInfo;
import com.uangel.scenario.Scenario;
import com.uangel.scenario.phases.LabelPhase;
import com.uangel.scenario.phases.LoopPhase;
import com.uangel.scenario.phases.OutgoingPhase;
import com.uangel.scenario.phases.SendPhase;
import com.uangel.scenario.type.OutMsgType;

/**
 * @author dajin kim
 */
public abstract class MsgBuilder {

    protected SessionInfo sessionInfo;
    protected final Scenario scenario;
    protected final OutMsgType type;

    protected MsgBuilder(Scenario scenario, OutMsgType type) {
        this.sessionInfo = null;
        this.scenario = scenario;
        this.type = type;
    }

    protected MsgBuilder(SessionInfo sessionInfo, OutMsgType type) {
        this.sessionInfo = sessionInfo;
        this.scenario = sessionInfo.getScenario();
        this.type = type;
    }

    protected boolean isSendType() {
        return OutMsgType.SEND.equals(type);
    }

    protected boolean checkType(OutgoingPhase phase) {
        if (isSendType()) {
            // Send 만 처리
            return phase instanceof SendPhase;
        } else if (OutMsgType.LOOP.equals(type)){
            // Loop, Label 만 처리
            return (phase instanceof LoopPhase) || (phase instanceof LabelPhase);
        }
        return false;
    }

    public abstract byte[] build(OutgoingPhase outgoingPhase);
}
