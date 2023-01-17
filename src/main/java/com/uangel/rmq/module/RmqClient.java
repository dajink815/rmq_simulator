package com.uangel.rmq.module;

import com.uangel.rmq.module.transport.RmqSender;
import com.uangel.rmq.util.PasswdDecryptor;
import com.uangel.service.ServiceDefine;
import lombok.extern.slf4j.Slf4j;

import static java.nio.charset.StandardCharsets.UTF_8;


@Slf4j
public class RmqClient {
    private boolean isConnected;
    private RmqSender rmqSender = null;

    private final String host;
    private final String user;
    private final String pass;
    private final String queueName;
    private final int port;

    public RmqClient(String host, String user, String pass, String queueName, int port) {
        this.host = host;
        this.user = user;
        this.pass = pass;
        this.queueName = queueName;
        this.port = port;
    }

    public boolean start() {
        RmqSender sender = createSender();
        if (sender != null) {
            setRmqSender(sender);
            setConnected(true);
        }

        return isConnected;
    }

    private RmqSender createSender() {
        PasswdDecryptor decryptor = new PasswdDecryptor(ServiceDefine.U_RMQ.getStr(), ServiceDefine.PW_ALG.getStr());
        String decPass = "";
        try {
            decPass = decryptor.decrypt0(this.pass);
        } catch (Exception e) {
            log.error("RMQ Password is not available ", e);
        }
        RmqSender sender = new RmqSender(host, user, decPass, port, queueName);

        log.debug("RmqSender [{}] -> [{}:{}]", queueName, host, user);
        if (!sender.connect()) sender = null;
        return sender;
    }

    public void closeSender() {
        if (rmqSender != null) {
            rmqSender.close();
        }
    }

    public boolean send(String msg) {
        return send(msg.getBytes(UTF_8));
    }

    public boolean send(byte[] msg) {
        RmqSender sender = getRmqSender();
        if (sender == null) {
            sender = createSender();
            if (sender == null) {
                return false;
            }

            setRmqSender(sender);
            setConnected(true);
        }

        if (!sender.isOpened() && !sender.connect())
            return false;

        return sender.send(msg);
    }

    public String getQueueName() {
        return this.queueName;
    }

    public void setConnected(boolean conn) {
        this.isConnected = conn;
    }

    public RmqSender getRmqSender() {
        return rmqSender;
    }

    public void setRmqSender(RmqSender rmqSender) {
        this.rmqSender = rmqSender;
    }
}
