package com.uangel.rmq;

import com.uangel.command.CommandInfo;
import com.uangel.rmq.handler.RmqJsonConsumer;
import com.uangel.rmq.handler.RmqProtoConsumer;
import com.uangel.rmq.module.RmqModule;
import com.uangel.rmq.util.PasswdDecryptor;
import com.uangel.rmq.util.RmqMsgPrinter;
import com.uangel.scenario.Scenario;
import com.uangel.service.ServiceDefine;
import com.uangel.util.StringUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

@Slf4j
public class RmqManager {
    private final Scenario scenario;
    private final CommandInfo config;
    private RmqModule serverModule;
    @Getter
    private RmqModule clientModule;

    public RmqManager(Scenario scenario) {
        this.scenario = scenario;
        this.config = scenario.getCmdInfo();
    }

    public boolean start() {
        try {
            log.info("[{}] RmqManager Start (TestMode:{})", scenario.getName(), config.isTestMode());
            prepClient();
            prepServer();
            return serverModule != null;
        } catch (Exception e) {
            log.error("RmqManager start Fail");
        }
        return false;
    }

    public void stop() {
        log.info("[{}] RmqManager Stop", scenario.getName());
        if (serverModule != null) {
            serverModule.close();
            serverModule = null;
        }
        if (clientModule != null) {
            clientModule.close();
            clientModule = null;
        }
    }

    private void prepClient() throws IOException, TimeoutException {
        if (clientModule == null) {
            String host = config.getRmqTargetHost();
            if (host.equals(config.getRmqHost())) {
                log.info("RMQ server, client host address is same. [{}]", host);
                return;
            }
            String target = config.getRmqTarget();
            String user = config.getRmqTargetUser();
            String pass = config.getRmqTargetPass();
            int port = config.getRmqTargetPort();
            clientModule = addRmqModule(target, host, user, pass, port);
        }
    }

    private void prepServer() throws IOException, TimeoutException {
        if (serverModule == null) {
            String target = config.getRmqLocal();
            String host = config.getRmqHost();
            String user = config.getRmqUser();
            String pass = config.getRmqPass();
            int port = config.getRmqPort();

            serverModule = addRmqModule(target, host, user, pass, port);
            serverModule.queueDeclare(target);
            serverModule.registerConsumer(target, this::handleRmqMessage);
        }
    }

    private RmqModule addRmqModule(String target, String host, String user, String pass, int port) throws IOException, TimeoutException {
        PasswdDecryptor decryptor = new PasswdDecryptor(ServiceDefine.U_RMQ.getStr(), ServiceDefine.PW_ALG.getStr());
        String decPass = decryptor.decrypt0(pass);
        return new RmqModule(host, user, decPass, port, config.getThreadSize(), config.getRmqQueueSize())
                .connect(() -> log.info("RabbitMQ [{}] Connect Success. [{}@{}:{}]", target, user, host, port),
                        () -> log.warn("RabbitMQ [{}] DisConnect. [{}@{}:{}]", target, user, host, port));
    }

    private void handleRmqMessage(byte[] msg) {
        if (scenario == null || scenario.isTestEnded()) return;

        if (scenario.isProtoType()) {
            RmqProtoConsumer protoConsumer = new RmqProtoConsumer(scenario);
            protoConsumer.protoMsgProcessing(msg);
        } else {
            RmqJsonConsumer jsonConsumer = new RmqJsonConsumer(scenario);
            jsonConsumer.jsonMsgProcessing(msg);
        }
    }

    public boolean send(String target, byte[] msg) {
        if (msg.length == 0) return false;
        if (serverModule == null) {
            log.warn("RMQ Module is null. Fail to send message. (target:{})", target);
            return false;
        }

        // default target queue name
        if (StringUtil.isNull(target)) target = config.getRmqTarget();
        Objects.requireNonNullElseGet(clientModule, () -> serverModule).sendMessage(target, msg);

        RmqMsgPrinter.printSendMsg(target, msg);
        return true;
    }


}
