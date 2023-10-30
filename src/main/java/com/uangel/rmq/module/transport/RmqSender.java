package com.uangel.rmq.module.transport;

import com.rabbitmq.client.AMQP;
import com.uangel.rmq.module.RmqInfo;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RmqSender extends RmqTransport {
    private final AMQP.BasicProperties properties;

    public RmqSender(RmqInfo rmqInfo, int expiration) {
        super(rmqInfo.getHost(), rmqInfo.getUser(), rmqInfo.getPass(), rmqInfo.getPort(), rmqInfo.getRmqName());
        properties = new AMQP.BasicProperties.Builder().expiration(Integer.toString(expiration)).build();
    }

    /**
     * RabbitMQ Publish
     *
     * @param msg RabbitMQ message
     * @return
     */
    public boolean send(byte[] msg) {
        if (!isConnected() || isBlocked()) {
            log.error("channel is not opened or not available. [RMQ name: {}]", getQueueName());
            return false;
        }

        try {
            getChannel().basicPublish("", getQueueName(), properties, msg);
            return true;
        } catch (Exception e) {
            log.error("RmqSender.send.basicPublish exception", e);
        }
        return false;
    }
}

