package com.uangel.rmq.module;

import com.uangel.rmq.handler.RmqConsumer;
import com.uangel.rmq.module.transport.RmqReceiver;
import com.uangel.rmq.util.PasswdDecryptor;
import com.uangel.scenario.Scenario;
import com.uangel.service.ServiceDefine;
import com.uangel.util.SleepUtil;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

@Slf4j
public class RmqServer {

    private RmqReceiver receiver = null;
    private final BlockingQueue<byte[]> queue;

    private final RmqInfo rmqInfo;
    private final Scenario scenario;
    private boolean isQuit = false;

    public RmqServer(RmqInfo rmqInfo, int recvQueueSize, Scenario scenario) {
        this.rmqInfo = rmqInfo;
        this.queue = new ArrayBlockingQueue<>(recvQueueSize);
        this.scenario = scenario;
    }

    public boolean start() {
        try {
            PasswdDecryptor decryptor = new PasswdDecryptor(ServiceDefine.U_RMQ.getStr(), ServiceDefine.PW_ALG.getStr());
            String decPass = decryptor.decrypt0(rmqInfo.getPass());
            rmqInfo.setPass(decPass);
        } catch (Exception e) {
            log.error("RMQ Password is not available ", e);
        }

        if (receiver == null) {
            receiver = new RmqReceiver(rmqInfo, new MessageCallback());
            log.debug("RmqReceiver [{}] -> [{}:{}]", rmqInfo.getRmqName(), rmqInfo.getHost(), rmqInfo.getUser());
            new RmqConsumer(queue, scenario).run();
        }

        boolean result = false;
        if (receiver.connect()) {
            result = receiver.start();
        } else {
            log.warn("SERVER({}) connectServer is fail.", rmqInfo.getRmqName());
            new Thread(new RmqConnectThread()).start();
        }

        return result;
    }

    public void stop() {
        isQuit = true;
        if (receiver != null) {
            receiver.close();
            receiver = null;
        }
    }

    private class MessageCallback implements RmqCallback {
        @Override
        public void onReceived(byte[] msg) {
            if (msg != null) {
                try {
                    if (!queue.offer(msg)) {
                        String json = new String(msg, StandardCharsets.UTF_8);
                        log.warn("RmqServer({}) onReceived Fail - msg was dropped\r\n{}", rmqInfo.getRmqName(), json);
                    }
                } catch (Exception e) {
                    log.warn("RMQ({}) RmqServer.onReceived.exception.", rmqInfo.getRmqName(), e);
                }
            }
        }
    }

    /**
     * RMQ connect 재시도
     * @author kangmooHeo
     */
    private class RmqConnectThread implements Runnable {
        @Override
        public void run() {
            while (!isQuit) {
                SleepUtil.trySleep(1000);
                if (!receiver.isConnected()) {
                    log.warn("SERVER({}) retry connect.", rmqInfo.getRmqName());
                    if (receiver.connect()) {
                        boolean result = receiver.start();
                        log.warn("SERVER({}) is connected {}", rmqInfo.getRmqName(), result);
                    }
                } else {
                    break;
                }
            }
        }
    }

}
