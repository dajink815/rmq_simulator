package com.uangel.rmq;

import com.uangel.command.CommandInfo;
import com.uangel.rmq.module.RmqClient;
import com.uangel.rmq.module.RmqInfo;
import com.uangel.rmq.module.RmqServer;
import com.uangel.scenario.Scenario;
import com.uangel.util.StringUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author dajin kim
 */
@Slf4j
public class RmqManager {
    private final Scenario scenario;
    private final CommandInfo config;
    private RmqServer rmqServer;
    @Getter
    private RmqClient rmqClient;

    public RmqManager(Scenario scenario) {
        this.scenario = scenario;
        this.config = scenario.getCmdInfo();
    }

    public boolean start() {
        log.info("[{}] RmqManager Start", scenario.getName());
        startRmqClient();
        startRmqServer();
        return (rmqServer != null) && (rmqClient != null);
    }

    public void stop() {
        log.info("[{}] RmqManager Stop", scenario.getName());
        if (rmqServer != null) {
            rmqServer.stop();
            rmqServer = null;
        }
        if (rmqClient != null) {
            rmqClient.closeSender();
            rmqClient = null;
        }
    }

    private void startRmqClient() {
        if (rmqClient == null) {
            String target = config.getRmqTarget();
            String host = config.getRmqTargetHost();
            String user = config.getRmqTargetUser();
            String pass = config.getRmqTargetPass();
            int port = config.getRmqTargetPort();
            RmqInfo rmqInfo = new RmqInfo(host, user, pass, target, port);

            RmqClient client = new RmqClient(rmqInfo, config.getRmqQueueSize());
            boolean result = client.start();
            if (result) rmqClient = client;
            log.info("RabbitMQ Client Start {}. [{}], [{}], [{}]", StringUtil.getSucFail(result), target, host, user);
        }
    }

    private void startRmqServer() {
        if (rmqServer == null) {
            String target = config.getRmqLocal();
            String host = config.getRmqHost();
            String user = config.getRmqUser();
            String pass = config.getRmqPass();
            int port = config.getRmqPort();
            RmqInfo rmqInfo = new RmqInfo(host, user, pass, target, port);

            RmqServer server = new RmqServer(rmqInfo, config.getRmqQueueSize(), scenario);
            boolean result = server.start();
            if (result) this.rmqServer = server;
            log.info("RabbitMQ Server Start {}. [{}], [{}], [{}]", StringUtil.getSucFail(result), target, host, user);
        }
    }


}
