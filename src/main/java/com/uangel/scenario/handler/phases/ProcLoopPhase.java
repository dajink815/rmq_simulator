package com.uangel.scenario.handler.phases;

import com.uangel.rmq.module.RmqClient;
import com.uangel.scenario.Scenario;
import com.uangel.scenario.handler.base.*;
import com.uangel.scenario.phases.LoopPhase;
import lombok.extern.slf4j.Slf4j;

/**
 * @author dajin kim
 */
@Slf4j
public class ProcLoopPhase {
    private final Scenario scenario;

    public ProcLoopPhase(Scenario scenario) {
        this.scenario = scenario;
    }

    public void run(LoopPhase loopPhase) {
        try {
            // create
            LoopMsgBuilder builder;
            if (scenario.isProtoType()) {
                builder = new LoopProtoMsgBuilder(scenario);
            } else {
                builder = new LoopJsonMsgBuilder(scenario);
            }
            byte[] msg = builder.build(loopPhase);

            log.debug("Session Count : [{}]", scenario.getSessionManager().getTotalSessionCnt());
            System.out.println("Session Count : " + scenario.getSessionManager().getTotalSessionCnt());

            // send
            RmqClient rmqClient = scenario.getRmqManager().getDefaultClient();
            rmqClient.send(msg);

        } catch (Exception e) {
            log.error("ProcLoopPhase.run.Exception ", e);
        }
    }
}
