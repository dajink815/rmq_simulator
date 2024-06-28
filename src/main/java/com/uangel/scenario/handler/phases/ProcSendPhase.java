package com.uangel.scenario.handler.phases;

import com.uangel.model.SessionInfo;
import com.uangel.scenario.handler.base.JsonMsgBuilder;
import com.uangel.scenario.handler.base.MsgBuilder;
import com.uangel.scenario.handler.base.ProtoMsgBuilder;
import com.uangel.scenario.phases.MsgPhase;
import com.uangel.scenario.phases.SendPhase;
import com.uangel.scenario.type.OutMsgType;
import lombok.extern.slf4j.Slf4j;

/**
 * @author dajin kim
 */
@Slf4j
public class ProcSendPhase extends ProcMsgPhase {

    public ProcSendPhase(SessionInfo sessionInfo) {
        super(sessionInfo);
    }

    @Override
    public void run(MsgPhase msgPhase) {
        SendPhase sendPhase = (SendPhase) msgPhase;

        try {
            // create
            MsgBuilder builder;
            if (scenario.isProtoType()) {
                builder = new ProtoMsgBuilder(sessionInfo, OutMsgType.SEND);
            } else {
                builder = new JsonMsgBuilder(sessionInfo, OutMsgType.SEND);
            }
            byte[] msg = builder.build(sendPhase);

            // send
            if (msg.length > 0) {
                String msgTarget = sendPhase.getTargetQueue();
                scenario.getRmqManager().send(msgTarget, msg);
            } else {
                log.warn("ProcSendPhase Fail - {} Idx Msg Build Fail (ProtoMode:{})", sessionInfo.getCurIdx(), scenario.isProtoType());
            }

            sessionInfo.execPhase(sessionInfo.increaseCurIdx());
        } catch (Exception e) {
            log.error("ProcSendPhase.run.Exception ", e);
        }

    }
}
