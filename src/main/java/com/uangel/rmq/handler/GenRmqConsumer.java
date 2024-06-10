package com.uangel.rmq.handler;

import com.uangel.scenario.Scenario;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import umedia.rmq.RmqConsumer;

@Slf4j
@Setter
public class GenRmqConsumer extends RmqConsumer {

    private final Scenario scenario;

    public GenRmqConsumer(int consumerCount, int rmqBufferCount, Scenario scenario) {
        super(consumerCount, rmqBufferCount);
        this.scenario = scenario;
    }

    @Override
    public void handleRmqMessage(byte[] msg) {
        if (scenario == null || scenario.isTestEnded()) return;

        if (scenario.isProtoType()) {
            RmqProtoConsumer protoConsumer = new RmqProtoConsumer(scenario);
            protoConsumer.protoMsgProcessing(msg);
        } else {
            RmqJsonConsumer jsonConsumer = new RmqJsonConsumer(scenario);
            jsonConsumer.jsonMsgProcessing(msg);
        }
    }
}
