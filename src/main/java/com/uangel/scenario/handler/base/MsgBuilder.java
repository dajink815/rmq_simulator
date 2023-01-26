package com.uangel.scenario.handler.base;

import com.uangel.scenario.phases.SendPhase;

/**
 * @author dajin kim
 */
public interface MsgBuilder {

    public byte[] build(SendPhase sendPhase);
}
