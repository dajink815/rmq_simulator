package com.uangel.scenario.handler.base;

import com.uangel.scenario.Scenario;
import com.uangel.scenario.phases.LoopPhase;
import lombok.extern.slf4j.Slf4j;

/**
 * @author dajin kim
 */
@Slf4j
public class LoopJsonMsgBuilder extends LoopMsgBuilder {

    public LoopJsonMsgBuilder(Scenario scenario) {
        super(scenario);
    }

    @Override
    public byte[] build(LoopPhase loopPhase) {
        return new byte[0];
    }
}
