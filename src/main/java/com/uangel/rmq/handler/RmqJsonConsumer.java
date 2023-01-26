package com.uangel.rmq.handler;

import com.uangel.scenario.Scenario;

/**
 * @author dajin kim
 */
public class RmqJsonConsumer {
    private final Scenario scenario;

    public RmqJsonConsumer(Scenario scenario) {
        this.scenario = scenario;
    }

    public void jsonMsgProcessing(byte[] msg) {
        if (scenario.isTestEnded()) return;



    }
}
