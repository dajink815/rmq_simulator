package com.uangel.scenario.handler.phases;

import com.uangel.model.SessionInfo;
import com.uangel.model.SessionManager;
import com.uangel.scenario.phases.MsgPhase;
import com.uangel.service.AppInstance;

/**
 * @author dajin kim
 */
public abstract class ProcMsgPhase {
    protected static final SessionManager sessionManager = SessionManager.getInstance();
    protected static final AppInstance instance = AppInstance.getInstance();
    protected final SessionInfo sessionInfo;

    protected ProcMsgPhase(SessionInfo sessionInfo) {
        this.sessionInfo = sessionInfo;
    }

    public abstract void run(MsgPhase msgPhase);

}
