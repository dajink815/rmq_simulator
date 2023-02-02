package com.uangel.scenario.handler.phases;

import com.uangel.rmq.module.RmqClient;
import com.uangel.scenario.Scenario;
import com.uangel.scenario.handler.base.LoopJsonMsgBuilder;
import com.uangel.scenario.handler.base.LoopMsgBuilder;
import com.uangel.scenario.handler.base.LoopProtoMsgBuilder;
import com.uangel.scenario.phases.LabelPhase;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Optional;

/**
 * @author dajin kim
 */
@Slf4j
public class ProcLabelPhase {
    private final Scenario scenario;

    public ProcLabelPhase(Scenario scenario) {
        this.scenario = scenario;
    }

    public void run(String id, Map<String, String> fields) {
        Optional.ofNullable(scenario.getLabelPhase(id)).ifPresent(labelPhase -> run(labelPhase, fields));
    }

    // 프로세스 전체 메시지 아니고 세션 별 label 메시지 처리시에는...? Session field 도 전달?
    public void run(LabelPhase labelPhase, Map<String, String> fields) {

        log.debug("ProcLabelPhase id [{}] fields: {}", labelPhase.getId(), fields);

        try {
            // create
            LoopMsgBuilder builder;
            if (scenario.isProtoType()) {
                builder = new LoopProtoMsgBuilder(scenario);
            } else {
                builder = new LoopJsonMsgBuilder(scenario);
            }
            byte[] msg = builder.build(labelPhase);

            // send
            RmqClient rmqClient = scenario.getRmqManager().getDefaultClient();
            rmqClient.send(msg);

        } catch (Exception e) {
            log.error("ProcLoopPhase.run.Exception ", e);
        }





    }

}
