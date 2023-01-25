package com.uangel.rmq;

import com.uangel.command.CommandInfo;
import com.uangel.rmq.handler.RmqConsumer;
import com.uangel.rmq.module.RmqClient;
import com.uangel.rmq.module.RmqServer;
import com.uangel.service.AppInstance;
import com.uangel.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;

import java.util.concurrent.*;

/**
 * @author dajin kim
 */
@Slf4j
public class RmqManager {
    private static RmqManager rmqManager = null;
    private final AppInstance instance = AppInstance.getInstance();
    private final CommandInfo config = instance.getCmdInfo();
    private ExecutorService executorRmqService;

    private RmqServer rmqServer;
    private final ConcurrentHashMap<String, RmqClient> rmqClientMap = new ConcurrentHashMap<>();

    private RmqManager() {
        // nothing
    }

    public static RmqManager getInstance() {
        if (rmqManager == null) {
            rmqManager = new RmqManager();
        }
        return rmqManager;
    }

    public void start() {
        log.info("[{}] RmqManager Start", instance.getScenario().getName());
        startRmqConsumer();
        startRmqClient();
        startRmqServer();
    }

    public void stop() {
        log.info("[{}] RmqManager Stop", instance.getScenario().getName());
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
                        .namingPattern("[" + instance.getScenario().getName() + "] RmqConsumer-%d")
                        //.namingPattern("RmqConsumer-%d")
                        .build());
        BlockingQueue<byte[]> rmqQueue = new ArrayBlockingQueue<>(config.getRmqQueueSize());
        instance.setRmqQueue(rmqQueue);

        for (int i = 0; i < config.getRmqThreadSize(); i++) {
            executorRmqService.execute(new RmqConsumer(rmqQueue));
        }
    }

    private void startRmqClient() {
        String target = config.getRmqTarget();
        String host = config.getRmqTargetHost();
        String user = config.getRmqTargetUser();
        String pass = config.getRmqTargetPass();
        int port = config.getRmqTargetPort();
        addClient(target, host, user, pass, port);
    }

    private void startRmqServer() {
        if (rmqServer == null) {
            String target = config.getRmqLocal();
            String host = config.getRmqHost();
            String user = config.getRmqUser();
            String pass = config.getRmqPass();
            int port = config.getRmqPort();

            RmqServer server = new RmqServer(host, user, pass, target, port);
            boolean result = server.start();
            if (result) this.rmqServer = server;
            log.info("RabbitMQ Server Start {}. [{}], [{}], [{}]", StringUtil.getSucFail(result), target, host, user);
        }
    }

    private boolean addClient(String target, String host, String user, String pass, int port) {
        boolean result = false;
        if (rmqClientMap.get(target) == null) {
            RmqClient client = new RmqClient(host, user, pass, target, port);
            result = client.start();
            if (result) rmqClientMap.put(target, client);
            log.info("RabbitMQ Client Start {}. [{}], [{}], [{}]", StringUtil.getSucFail(result), target, host, user);
        }
        return result;
    }

    public RmqClient getRmqClient(String queueName) {
        return rmqClientMap.get(queueName);
    }

    public RmqServer getRmqServer() {
        return rmqServer;
    }
}
