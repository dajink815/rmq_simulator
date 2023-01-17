package com.uangel.rmq.module.transport;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.uangel.rmq.module.RmqCallback;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

@Slf4j
public class RmqReceiver extends RmqTransport {

    private RmqCallback callback = null;

    /**
     * RabbitMQ Message Receiver
     *
     * @param host      RabbitMQ host
     * @param userName  RabbitMQ user
     * @param password  RabbitMQ password(암호화)
     * @param port      RabbitMQ post
     * @param queueName RabbitMQ a2s queueName
     */
    public RmqReceiver(String host, String userName, String password, int port, String queueName) {
        super(host, userName, password, port, queueName);
    }

    public void setCallback(RmqCallback callback) {
        this.callback = callback;
    }

    private final Consumer consumer = new DefaultConsumer(getChannel()) {
        @Override
        public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
            if (callback != null) {
                try {
                    Date ts = null;
                    Map<String, Object> headers = properties.getHeaders();
                    if (headers != null) {
                        // rmq message log message 도착 시간
                        Long ms = (Long) headers.get("timestamp_in_ms");
                        if (ms != null) {
                            ts = new Date(ms);
                        }
                    }
                    callback.onReceived(body, ts);
                } catch (Exception e) {
                    log.error("RmqReceiver.handleDelivery", e);
                }
            }
        }
    };



    public boolean start() {

        if (!getChannel().isOpen()) {
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
