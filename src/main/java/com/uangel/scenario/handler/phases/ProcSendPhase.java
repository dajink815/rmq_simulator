package com.uangel.scenario.handler.phases;

import com.uangel.model.SessionInfo;
import com.uangel.rmq.RmqManager;
import com.uangel.rmq.module.RmqClient;
import com.uangel.scenario.handler.base.JsonMsgBuilder;
import com.uangel.scenario.handler.base.MsgBuilder;
import com.uangel.scenario.handler.base.ProtoMsgBuilder;
import com.uangel.scenario.phases.MsgPhase;
import com.uangel.scenario.phases.SendPhase;
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
            if (instance.isProtoType()) {
                builder = new ProtoMsgBuilder(sessionInfo);
            } else {
                builder = new JsonMsgBuilder();
            }
            byte[] msg = builder.build(sendPhase);

            // send
            RmqClient rmqClient = RmqManager.getInstance().getDefaultClient();
            rmqClient.send(msg);
        } catch (Exception e) {
            log.error("ProcSendPhase.run.Exception ", e);
        }

    }
}
