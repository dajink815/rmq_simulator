package com.uangel.rmq.handler;

import com.uangel.service.AppInstance;

/**
 * @author dajin kim
 */
public class RmqJsonConsumer {

    private final AppInstance appInstance = AppInstance.getInstance();

    public RmqJsonConsumer() {
        // nothing
    }

    public void jsonMsgProcessing(byte[] msg) {
        if (appInstance.isTestEnded()) return;



    }
}
