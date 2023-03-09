package com.uangel.rmq.handler;

import com.uangel.scenario.Scenario;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author dajin kim
 */
@Slf4j
public class RmqConsumer {
    private final Scenario scenario;
    private final BlockingQueue<byte[]> rmqQueue;
    private boolean isQuit = false;

    public RmqConsumer(BlockingQueue<byte[]> queue, Scenario scenario) {
        this.rmqQueue = queue;
        this.scenario = scenario;
    }

    public RmqConsumer run() {
        if (scenario.getExecutorService() == null) return null;
        scenario.getExecutorService().scheduleWithFixedDelay(this::queueProcessing, 20, 20, TimeUnit.MILLISECONDS);
        return this;
    }

    private void queueProcessing() {
        // 시나리오 종료 체크
        if (scenario.isTestEnded()) return;

        while (!isQuit) {
            try {
                byte[] msg = rmqQueue.poll();
                if (msg == null) break;
                msgProcessing(msg);
            } catch (Exception e) {
                log.error("RmqConsumer.queueProcessing", e);
                isQuit = true;
            }
        }
    }

    private void msgProcessing(byte[] msg) {
        if (scenario.isProtoType()) {
            RmqProtoConsumer protoConsumer = new RmqProtoConsumer(scenario);
            protoConsumer.protoMsgProcessing(msg);
        } else {
            RmqJsonConsumer jsonConsumer = new RmqJsonConsumer(scenario);
            jsonConsumer.jsonMsgProcessing(msg);
        }

    }
}
