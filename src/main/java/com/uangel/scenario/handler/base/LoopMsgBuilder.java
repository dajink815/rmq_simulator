package com.uangel.scenario.handler.base;

import com.uangel.scenario.Scenario;
import com.uangel.scenario.phases.OutgoingPhase;

/**
 * @author dajin kim
 */
public abstract class LoopMsgBuilder {

    protected final Scenario scenario;

    protected LoopMsgBuilder(Scenario scenario) {
        this.scenario = scenario;
    }

    public abstract byte[] build(OutgoingPhase loopPhase);

}
