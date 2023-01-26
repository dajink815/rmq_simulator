package com.uangel.scenario.handler.base;

import com.uangel.scenario.phases.SendPhase;

/**
 * @author dajin kim
 */
public class JsonMsgBuilder implements MsgBuilder {

    public JsonMsgBuilder() {
        // nothing
    }

    @Override
    public byte[] build(SendPhase sendPhase) {
        return new byte[0];
    }
}
