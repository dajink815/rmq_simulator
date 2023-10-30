package com.uangel.rmq.module;

import com.uangel.rmq.module.transport.RmqSender;
import com.uangel.rmq.util.PasswdDecryptor;
import com.uangel.service.ServiceDefine;
import com.uangel.util.SleepUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import static java.nio.charset.StandardCharsets.UTF_8;


@Slf4j
public class RmqClient {
    private boolean isConnected;
    private boolean isQuit = false;

    private RmqSender sender = null;

    private final RmqInfo rmqInfo;
    private final BlockingQueue<byte[]> queue;

    public RmqClient(RmqInfo rmqInfo, int sendQueueSize) {
        this.rmqInfo = rmqInfo;
        this.queue = new ArrayBlockingQueue<>(sendQueueSize);
    }

    public boolean start() {
        try {
            PasswdDecryptor decryptor = new PasswdDecryptor(ServiceDefine.U_RMQ.getStr(), ServiceDefine.PW_ALG.getStr());
            String decPass = decryptor.decrypt0(rmqInfo.getPass());
            rmqInfo.setPass(decPass);
        } catch (Exception e) {
            log.error("RMQ Password is not available ", e);
        }

        if (sender == null) {
            sender = new RmqSender(rmqInfo, 5000);
        }

        isConnected = sender.connect();

        if (isConnected) {
            log.info("RMQ({}) is connected", rmqInfo.getRmqName());
            new Thread(new RmqSenderProc()).start();
        } else {
            log.info("RMQ({}) is disconnected", rmqInfo.getRmqName());
            new Thread(new RmqConnectThread()).start();
        }

        return isConnected;
    }

    public void closeSender() {
        isQuit = true;
        if (sender != null) {
            sender.close();
            sender = null;
        }
    }

    public boolean send(String msg) {
        return send(msg.getBytes(UTF_8));
    }

    public boolean send(byte[] msg) {
        if (sender == null) {
            log.warn("RMQ({}) sender is null. message is ignored.", rmqInfo.getRmqName());
            return false;
        }

        boolean result = false;
        try {
            if (!queue.offer(msg)){
                log.warn("RmqClient({}) send Fail - msg was dropped\r\n{}", rmqInfo.getRmqName(), msg);
            } else {
                result = true;
            }
        } catch (Exception e) {
            log.warn("RMQ({}) RmqClient.send.exception.", rmqInfo.getRmqName(), e);
        }

        return result;
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
                if (sender != null && !sender.isConnected()) {
                    isConnected = sender.connect();
                    if (isConnected) {
                        log.info("RMQ({}) is connect.", rmqInfo.getRmqName());
                        new Thread(new RmqSenderProc()).start();
                        return;
                    } else {
                        log.error("RMQ({}) is disconnect.", rmqInfo.getRmqName());
                    }
                }
            }
        }
    }

    /**
     * Send RMQ MSG
     * @author kangmooHeo
     */
    private class RmqSenderProc implements Runnable {
        @Override
        public void run() {
            while (!isQuit) {
                try {
                    byte[] msg = queue.poll(20, TimeUnit.MILLISECONDS);
                    if (msg == null) {
                        SleepUtil.trySleep(20);
                    } else {
                        sender.send(msg);
                    }
                } catch (InterruptedException e) {
                    log.warn("RMQ({}) client thread InterruptedException.", rmqInfo.getRmqName(), e);
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

}
