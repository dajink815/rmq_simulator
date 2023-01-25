package com.uangel.rmq.handler;

import com.uangel.service.AppInstance;
import com.uangel.util.SleepUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author dajin kim
 */
@Slf4j
public class RmqConsumer implements Runnable {
    private final AppInstance instance = AppInstance.getInstance();
    private final BlockingQueue<byte[]> rmqQueue;
    private boolean isQuit = false;

    public RmqConsumer(BlockingQueue<byte[]> queue) {
        this.rmqQueue = queue;
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
        if (instance.isProtoType()) {
            RmqProtoConsumer protoConsumer = new RmqProtoConsumer();
            protoConsumer.protoMsgProcessing(msg);
        } else {
            RmqJsonConsumer jsonConsumer = new RmqJsonConsumer();
            jsonConsumer.jsonMsgProcessing(msg);
        }

    }
}
