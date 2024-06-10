package com.uangel.scenario.handler.phases;

import com.uangel.model.SessionManager;
import com.uangel.scenario.Scenario;
import com.uangel.scenario.handler.base.*;
import com.uangel.scenario.phases.LoopPhase;
import com.uangel.scenario.type.OutMsgType;
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
            MsgBuilder builder;
            if (scenario.isProtoType()) {
                builder = new ProtoMsgBuilder(scenario, OutMsgType.LOOP);
            } else {
                builder = new JsonMsgBuilder(scenario, OutMsgType.LOOP);
            }
            byte[] msg = builder.build(loopPhase);

            SessionManager sessionManager = scenario.getSessionManager();
            log.info("Session Count : [{}] [Total:{}]", sessionManager.getCurrentSessionCnt(), sessionManager.getTotalSessionCnt());
            System.out.println("Session Count : " + sessionManager.getCurrentSessionCnt() + " (Total:" + sessionManager.getTotalSessionCnt() + ")");

            // send
            String target = loopPhase.getTargetQueue();
            scenario.getGenRmqManager().send(target, msg);

        } catch (Exception e) {
            log.error("ProcLoopPhase.run.Exception ", e);
        }
    }
}
