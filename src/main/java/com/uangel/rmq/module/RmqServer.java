package com.uangel.rmq.module;

import com.uangel.rmq.module.transport.RmqReceiver;
import com.uangel.rmq.util.PasswdDecryptor;
import com.uangel.service.ServiceDefine;
import com.uangel.util.Suppress;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.concurrent.BlockingQueue;

@Slf4j
public class RmqServer {
    private static final Suppress suppr = new Suppress(1000L * 30);

    private RmqReceiver rmqReceiver = null;
    private final BlockingQueue<byte[]> queue;

    private final String host;
    private final String user;
    private final String pass;
    private final String queueName;
    private final int port;

    public RmqServer(String host, String user, String pass, String queueName, int port, BlockingQueue<byte[]> rmqQueue) {
        this.host = host;
        this.user = user;
        this.pass = pass;
        this.queueName = queueName;
        this.port = port;
        this.queue = rmqQueue;
    }

    public boolean start() {
        PasswdDecryptor decryptor = new PasswdDecryptor(ServiceDefine.U_RMQ.getStr(), ServiceDefine.PW_ALG.getStr());
        String decPass = "";

        try {
            decPass = decryptor.decrypt0(this.pass);
        } catch (Exception e) {
            log.error("RMQ Password is not available ", e);
        }

        RmqReceiver receiver = new RmqReceiver(this.host, this.user, decPass, this.port, this.queueName);
        receiver.setCallback(new MessageCallback());
        log.debug("RmqReceiver [{}] -> [{}:{}]", queueName, host, user);

        boolean result = false;
        if (receiver.connect()) {
            setRmqReceiver(receiver);
            result = receiver.start();
        }

        return result;
    }

    public void stop() {
        if (rmqReceiver != null)
            rmqReceiver.close();
    }

    public void setRmqReceiver(RmqReceiver rmqReceiver) {
        this.rmqReceiver = rmqReceiver;
    }

    private class MessageCallback implements RmqCallback {
        @Override
        public void onReceived(byte[] msg, Date ts) {
            String prettyMsg = null;
           // RmqMessage rmqMsg = null;
            try {
                //prettyMsg = JsonUtil.buildPretty(msg);
                //rmqMsg = JsonUtil.parse(msg, RmqMessage.class);
            } catch (Exception e) {
                log.error("RmqServer.parseMessage", e);
            }

            // if (rmqMsg == null) return;

            log.debug("Received MSG [{}]", new String(msg));

            try {
                queue.put(msg);
            } catch (InterruptedException e) {
                log.error("MessageCallback.onReceived", e);
                Thread.currentThread().interrupt();
            }

        }



    }

}
