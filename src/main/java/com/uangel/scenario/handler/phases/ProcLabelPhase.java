package com.uangel.scenario.handler.phases;

import com.uangel.scenario.Scenario;
import com.uangel.scenario.handler.base.JsonMsgBuilder;
import com.uangel.scenario.handler.base.MsgBuilder;
import com.uangel.scenario.handler.base.ProtoMsgBuilder;
import com.uangel.scenario.phases.LabelPhase;
import com.uangel.scenario.type.OutMsgType;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Optional;

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

        log.trace("ProcLabelPhase id [{}]", labelPhase.getId());

        try {
            // 타입 별 Builder
            MsgBuilder builder;
            if (scenario.isProtoType()) {
                builder = new ProtoMsgBuilder(scenario, OutMsgType.LOOP);
            } else {
                builder = new JsonMsgBuilder(scenario, OutMsgType.LOOP);
            }

            // build
            byte[] msg = builder.build(labelPhase);

            // send
            String target = labelPhase.getTargetQueue();
            scenario.getRmqManager().send(target, msg);

        } catch (Exception e) {
            log.error("ProcLoopPhase.run.Exception ", e);
        }

    }

}
