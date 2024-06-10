package com.uangel.rmq;

import com.uangel.command.CommandInfo;
import com.uangel.rmq.handler.GenRmqConsumer;
import com.uangel.rmq.util.PasswdDecryptor;
import com.uangel.scenario.Scenario;
import com.uangel.service.ServiceDefine;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import umedia.rmq.RmqModule;

import java.util.Objects;

/**
 * @author dajin kim
 */
@Slf4j
public class GenRmqManager {
    private final Scenario scenario;
    private final CommandInfo config;
    private final RmqManager rmqManager = RmqManager.getInstance();
    private RmqModule serverModule;
    @Getter
    private RmqModule clientModule;

    public GenRmqManager(Scenario scenario) {
        this.scenario = scenario;
        this.config = scenario.getCmdInfo();
    }

    public boolean start() {
        try {
            log.info("[{}] GenRmqManager Start", scenario.getName());
            prepClient();
            prepServer();
            rmqManager.start(null);
            return serverModule != null;
        } catch (Exception e) {
            log.error("GenRmqManager start Fail");
        }
        return false;
    }

    public void stop() {
        log.info("[{}] GenRmqManager Stop", scenario.getName());
        if (serverModule != null) {
            serverModule.close();
            serverModule = null;
        }
        if (clientModule != null) {
            clientModule.close();
            clientModule = null;
        }
    }

    private void prepClient() {
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

            PasswdDecryptor decryptor = new PasswdDecryptor(ServiceDefine.U_RMQ.getStr(), ServiceDefine.PW_ALG.getStr());
            String decPass = decryptor.decrypt0(pass);

            this.clientModule = new RmqModule(host, user, decPass, port, config.getRmqQueueSize());
            rmqManager.addRmqModule(target, clientModule, config.getRmqLocal(),
                    () -> log.info("RabbitMQ Client [{}] Connect Success. [{}@{}:{}]", target, user, host, port),
                    () -> log.warn("RabbitMQ Client [{}] DisConnect. [{}@{}:{}]", target, user, host, port),
                    null);
        }
    }

    private void prepServer() {
        if (serverModule == null) {
            String target = config.getRmqLocal();
            String host = config.getRmqHost();
            String user = config.getRmqUser();
            String pass = config.getRmqPass();
            int port = config.getRmqPort();

            PasswdDecryptor decryptor = new PasswdDecryptor(ServiceDefine.U_RMQ.getStr(), ServiceDefine.PW_ALG.getStr());
            String decPass = decryptor.decrypt0(pass);
            GenRmqConsumer genRmqConsumer = new GenRmqConsumer(config.getThreadSize(), config.getRmqQueueSize(), scenario);

            this.serverModule = new RmqModule(host, user, decPass, port, config.getRmqQueueSize());
            rmqManager.addRmqModule(target, serverModule, target,
                    () -> log.info("RabbitMQ Server [{}] Connect Success. [{}@{}:{}]", target, user, host, port),
                    () -> log.warn("RabbitMQ Server [{}] DisConnect. [{}@{}:{}]", target, user, host, port),
                    genRmqConsumer);
        }
    }

    public boolean send(byte[] msg) {
        return send(null, msg);
    }

    public boolean send(String target, byte[] msg) {
        if (serverModule == null) {
            log.warn("RMQ Module is null. Fail to send message. (target:{})", target);
            return false;
        }

        // default target queue name
        if (target == null || target.isEmpty())
            target = config.getRmqTarget();
        Objects.requireNonNullElseGet(clientModule, () -> serverModule).sendMessage(target, msg);
        return true;
    }


}
