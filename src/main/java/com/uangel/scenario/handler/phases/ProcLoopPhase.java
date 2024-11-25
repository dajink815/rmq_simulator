package com.uangel.scenario.handler.phases;

import com.uangel.scenario.Scenario;
import com.uangel.scenario.handler.base.*;
import com.uangel.scenario.phases.LoopPhase;
import com.uangel.scenario.type.OutMsgType;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProcLoopPhase {
    private final Scenario scenario;

    public ProcLoopPhase(Scenario scenario) {
        this.scenario = scenario;
    }

    public void run(LoopPhase loopPhase) {
        try {
            // 타입 별 Builder
            MsgBuilder builder;
            if (scenario.isProtoType()) {
                builder = new ProtoMsgBuilder(scenario, OutMsgType.LOOP);
            } else {
                builder = new JsonMsgBuilder(scenario, OutMsgType.LOOP);
            }

            // build
            byte[] msg = builder.build(loopPhase);

            // send
            String target = loopPhase.getTargetQueue();
            scenario.getRmqManager().send(target, msg);

        } catch (Exception e) {
            log.error("ProcLoopPhase.run.Exception ", e);
        }
    }
}
