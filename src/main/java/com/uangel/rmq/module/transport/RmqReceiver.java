package com.uangel.rmq.module.transport;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.uangel.rmq.module.RmqCallback;
import com.uangel.rmq.module.RmqInfo;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RmqReceiver extends RmqTransport {

    private final RmqCallback callback;

    public RmqReceiver(RmqInfo rmqInfo, RmqCallback callback) {
        super(rmqInfo.getHost(), rmqInfo.getUser(), rmqInfo.getPass(), rmqInfo.getPort(), rmqInfo.getRmqName());
        this.callback = callback;
    }

    private final Consumer consumer = new DefaultConsumer(getChannel()) {
        @Override
        public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) {
            if (callback == null) return;

            try {
                callback.onReceived(body);
            } catch (Exception e) {
                log.error("RmqReceiver.handleDelivery", e);
            }
        }
    };

    public boolean start() {

        if (!getChannel().isOpen()) {
            log.error("channel is not opened. [RMQ name: {}]", getQueueName());
            return false;
        }

        boolean result = false;

        try {
            getChannel().basicConsume(getQueueName(), true, this.consumer);
            result = true;
        } catch (Exception e) {
            log.error("RmqReceiver.start", e);
        }

        return result;
    }

}
