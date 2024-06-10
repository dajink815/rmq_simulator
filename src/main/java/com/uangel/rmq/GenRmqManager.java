package com.uangel.rmq;

import com.uangel.command.CommandInfo;
import com.uangel.rmq.handler.GenRmqConsumer;
import com.uangel.rmq.module.RmqClient;
import com.uangel.rmq.module.RmqServer;
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
    private RmqServer rmqServer;
    private RmqModule serverModule;
    @Getter
    private RmqClient rmqClient;
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

/*            RmqInfo rmqInfo = new RmqInfo(host, user, pass, target, port);

            RmqClient client = new RmqClient(rmqInfo, config.getRmqQueueSize());
            boolean result = client.start();
            if (result) rmqClient = client;
            log.info("RabbitMQ Client Start {}. [{}], [{}], [{}]", StringUtil.getSucFail(result), target, host, user);
      */
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
/*
            RmqInfo rmqInfo = new RmqInfo(host, user, pass, target, port);

            RmqServer server = new RmqServer(rmqInfo, config.getRmqQueueSize(), scenario);
            boolean result = server.start();
            if (result) this.rmqServer = server;
            log.info("RabbitMQ Server Start {}. [{}], [{}], [{}]", StringUtil.getSucFail(result), target, host, user);
      */
        }
    }

    public boolean send(byte[] msg) {
        return send(null, msg);
    }

    public boolean send(String target, byte[] msg) {
        if (serverModule == null) {
            log.warn("RMQ Client is null. message was ignored. (target:{})", target);
            return false;
        }

        if (target == null || target.isEmpty())
            target = config.getRmqTarget();
        Objects.requireNonNullElseGet(clientModule, () -> serverModule).sendMessage(target, msg);
        return true;
    }


}
