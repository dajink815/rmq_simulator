package com.uangel.scenario.handler.phases;

import com.uangel.model.SessionInfo;
import com.uangel.model.SessionManager;
import com.uangel.scenario.Scenario;
import com.uangel.scenario.phases.MsgPhase;

/**
 * @author dajin kim
 */
public abstract class ProcMsgPhase {
    protected final SessionInfo sessionInfo;
    protected final Scenario scenario;
    protected final SessionManager sessionManager;

    protected ProcMsgPhase(SessionInfo sessionInfo) {
        this.sessionInfo = sessionInfo;
        this.scenario = sessionInfo.getScenario();
        this.sessionManager = scenario.getSessionManager();
    }

    public abstract void run(MsgPhase msgPhase);

}
