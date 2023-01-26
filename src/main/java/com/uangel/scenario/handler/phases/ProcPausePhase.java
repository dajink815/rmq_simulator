package com.uangel.scenario.handler.phases;

import com.uangel.model.SessionInfo;
import com.uangel.scenario.phases.MsgPhase;
import com.uangel.scenario.phases.PausePhase;

/**
 * @author dajin kim
 */
public class ProcPausePhase extends ProcMsgPhase {

    public ProcPausePhase(SessionInfo sessionInfo) {
        super(sessionInfo);
    }

    @Override
    public void run(MsgPhase msgPhase) {
        PausePhase pausePhase = (PausePhase) msgPhase;
        int duration = pausePhase.getMilliSeconds();
        scenario.schedule(() -> sessionInfo.execPhase(sessionInfo.increaseCurIdx()), duration);
    }
}
