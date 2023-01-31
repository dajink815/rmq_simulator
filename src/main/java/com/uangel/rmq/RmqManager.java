package com.uangel.rmq;

import com.uangel.command.CommandInfo;
import com.uangel.rmq.handler.RmqConsumer;
import com.uangel.rmq.module.RmqClient;
import com.uangel.rmq.module.RmqServer;
import com.uangel.scenario.Scenario;
import com.uangel.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author dajin kim
 */
@Slf4j
public class RmqManager {
    private final Scenario scenario;
    private final CommandInfo config;
    private ExecutorService executorRmqService;
    private BlockingQueue<byte[]> rmqQueue;
    private RmqServer rmqServer;
    private final ConcurrentHashMap<String, RmqClient> rmqClientMap = new ConcurrentHashMap<>();

    private final AtomicBoolean isRunning = new AtomicBoolean(false);

    public RmqManager(Scenario scenario) {
        this.scenario = scenario;
        this.config = scenario.getCmdInfo();
    }

    public boolean start() {
        if (isRunning.get()) {
            log.warn("[{}] RmqManager is already started", scenario.getName());
            return false;
        }
        isRunning.set(true);
        log.info("[{}] RmqManager Start", scenario.getName());
        startRmqConsumer();
        boolean clientResult = startRmqClient();
        boolean serverResult = startRmqServer();
        return clientResult && serverResult;
    }

    public void stop() {
        if (!isRunning.get()) {
            log.info("[{}] RmqManager is already stopped", scenario.getName());
            return;
        }
        isRunning.set(false);
        log.info("[{}] RmqManager Stop", scenario.getName());
        if (rmqServer != null) {
            rmqServer.stop();
        }
        if (!rmqClientMap.isEmpty()) {
            rmqClientMap.forEach((key, client) -> client.closeSender());
        }
        if (executorRmqService != null) {
            executorRmqService.shutdown();
        }
    }

    private void startRmqConsumer() {
        if (executorRmqService != null) return;

        // UScheduler
        executorRmqService = Executors.newFixedThreadPool(config.getRmqThreadSize(),
                new BasicThreadFactory.Builder()
                        .namingPattern("[" + scenario.getName() + "] RmqConsumer-%d")
                        //.namingPattern("RmqConsumer-%d")
                        .build());
        rmqQueue = new ArrayBlockingQueue<>(config.getRmqQueueSize());

        for (int i = 0; i < config.getRmqThreadSize(); i++) {
            executorRmqService.execute(new RmqConsumer(rmqQueue, scenario));
        }
    }

    private boolean startRmqClient() {
        String target = config.getRmqTarget();
        String host = config.getRmqTargetHost();
        String user = config.getRmqTargetUser();
        String pass = config.getRmqTargetPass();
        int port = config.getRmqTargetPort();
        return addClient(target, host, user, pass, port);
    }

    private boolean startRmqServer() {
        boolean result = false;
        if (rmqServer == null) {
            String target = config.getRmqLocal();
            String host = config.getRmqHost();
            String user = config.getRmqUser();
            String pass = config.getRmqPass();
            int port = config.getRmqPort();

            RmqServer server = new RmqServer(host, user, pass, target, port, rmqQueue);
            result = server.start();
            if (result) this.rmqServer = server;
            log.info("RabbitMQ Server Start {}. [{}], [{}], [{}]", StringUtil.getSucFail(result), target, host, user);
        }
        return result;
    }

    private boolean addClient(String target, String host, String user, String pass, int port) {
        boolean result = false;
        if (StringUtil.notNull(target) && rmqClientMap.get(target) == null) {
            RmqClient client = new RmqClient(host, user, pass, target, port);
            result = client.start();
            if (result) rmqClientMap.put(target, client);
            log.info("RabbitMQ Client Start {}. [{}], [{}], [{}]", StringUtil.getSucFail(result), target, host, user);
        }
        return result;
    }

    public RmqClient getDefaultClient() {
        return rmqClientMap.get(config.getRmqTarget());
    }

    public RmqClient getRmqClient(String queueName) {
        return rmqClientMap.get(queueName);
    }

    public RmqServer getRmqServer() {
        return rmqServer;
    }
}
