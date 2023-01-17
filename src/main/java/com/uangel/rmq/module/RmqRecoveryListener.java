package com.uangel.rmq.module;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Recoverable;
import com.rabbitmq.client.RecoveryListener;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RmqRecoveryListener implements RecoveryListener {
    private final String queueName;

    public RmqRecoveryListener(String queueName) {
        this.queueName = queueName;
    }

    @Override
    public void handleRecovery(Recoverable recoverable) {
        if (recoverable instanceof Channel) {
            int channelNumber = ((Channel) recoverable).getChannelNumber();
            log.error("Rmq {} Connection to channel # {} was recovered.", queueName, channelNumber);
        }
    }

    @Override
    public void handleRecoveryStarted(Recoverable recoverable) {
        log.error("Rmq {} handleRecoveryStarted", queueName);
    }
}
