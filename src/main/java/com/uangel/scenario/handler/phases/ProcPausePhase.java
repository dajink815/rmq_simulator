package com.uangel.scenario.handler.phases;

import com.uangel.model.SessionInfo;
import com.uangel.scenario.phases.MsgPhase;

/**
 * @author dajin kim
 */
public class ProcPausePhase extends ProcMsgPhase {

    public ProcPausePhase(SessionInfo sessionInfo) {
        super(sessionInfo);
    }

    @Override
    public void run(MsgPhase msgPhase) {

    }
}
