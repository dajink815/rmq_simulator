package com.uangel.rmq.handler;

import com.uangel.scenario.Scenario;
import com.uangel.util.SleepUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author dajin kim
 */
@Slf4j
public class RmqConsumer implements Runnable {
    private final Scenario scenario;
    private final BlockingQueue<byte[]> rmqQueue;
    private boolean isQuit = false;

    public RmqConsumer(BlockingQueue<byte[]> queue, Scenario scenario) {
        this.rmqQueue = queue;
        this.scenario = scenario;
    }

    @Override
    public void run() {
        queueProcessing();
    }

    private void queueProcessing() {
        while (!isQuit) {
            try {
                byte[] msg = rmqQueue.poll(10, TimeUnit.MILLISECONDS);

                if (msg == null) {
                    SleepUtil.trySleep(10);
                    continue;
                }

                msgProcessing(msg);

            } catch (InterruptedException e) {
                log.error("RmqConsumer.queueProcessing", e);
                isQuit = true;
                Thread.currentThread().interrupt();
            }
        }
    }

    private void msgProcessing(byte[] msg) {
        if (scenario.isProtoType()) {
            RmqProtoConsumer protoConsumer = new RmqProtoConsumer(scenario);
            protoConsumer.protoMsgProcessing(msg);
        } else {
            // Json 은 byte 바로 String 변환해서 사용 : String msg = new String(msg, UTF_8);
            RmqJsonConsumer jsonConsumer = new RmqJsonConsumer(scenario);
            jsonConsumer.jsonMsgProcessing(msg);
        }

    }
}
