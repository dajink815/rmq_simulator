package com.uangel.scenario.handler.base;

import com.uangel.model.SessionInfo;
import com.uangel.scenario.Scenario;
import com.uangel.scenario.phases.SendPhase;

/**
 * @author dajin kim
 */
public abstract class MsgBuilder {

    protected final SessionInfo sessionInfo;
    protected final Scenario scenario;

    protected MsgBuilder(SessionInfo sessionInfo) {
        this.sessionInfo = sessionInfo;
        this.scenario = sessionInfo.getScenario();
    }

    public abstract byte[] build(SendPhase sendPhase);
}
