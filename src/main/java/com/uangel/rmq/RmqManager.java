package com.uangel.rmq;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import umedia.rmq.RmqConsumer;
import umedia.rmq.RmqModule;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * @author Kangmoo Heo
 */
@Slf4j
public class RmqManager {
    private static final RmqManager INSTANCE = new RmqManager();
    private final ConcurrentHashMap<String, RmqInfo> rmqInfos = new ConcurrentHashMap<>();
    private final AtomicBoolean isRunning = new AtomicBoolean();

    public RmqManager() {
    }

    public static RmqManager getInstance() {
        return INSTANCE;
    }

    /**
     * 새로운 RabbitMQ 모듈을 매니저에 추가한다.
     *
     * @param key            RMQ 모듈에 대한 고유 식별자.
     * @param rmqModule      RMQ 모듈의 인스턴스.
     * @param consumerQueue  Consumer Queue 이름.
     * @param onConnected    연결됐을 때 실행될 콜백.
     * @param onDisconnected 연결이 끊겼을 때 실행될 콜백.
     */
    public void addRmqModule(String key, RmqModule rmqModule, String consumerQueue, Runnable onConnected, Runnable onDisconnected) {
        if (rmqInfos.putIfAbsent(key, new RmqInfo(rmqModule, consumerQueue, onConnected, onDisconnected, null)) != null) {
            log.warn("The key already exists in the map. [{}]", key);
        }
    }

    /**
     * 새로운 RabbitMQ 모듈을 매니저에 추가한다.
     *
     * @param key            RMQ 모듈에 대한 고유 식별자.
     * @param rmqModule      RMQ 모듈의 인스턴스.
     * @param consumerQueue  Consumer Queue 이름.
     * @param onConnected    연결됐을 때 실행될 콜백.
     * @param onDisconnected 연결이 끊겼을 때 실행될 콜백.
     * @param rmqConsumer    RMQ 소비자.
     */
    public void addRmqModule(String key, RmqModule rmqModule, String consumerQueue, Runnable onConnected, Runnable onDisconnected, RmqConsumer rmqConsumer) {
        if (rmqInfos.putIfAbsent(key, new RmqInfo(rmqModule, consumerQueue, onConnected, onDisconnected, rmqConsumer)) != null) {
            log.warn("The key already exists in the map. [{}]", key);
        }
    }

    /**
     * 지정된 RabbitMQ 모듈을 제거하고 RmqModule을 닫는다.
     *
     * @param key 제거될 RMQ 모듈의 고유 식별자.
     */
    public RmqInfo removeRmqModule(String key) {
        RmqInfo rmqInfo = rmqInfos.remove(key);
        if (rmqInfo != null) {
            try {
                rmqInfo.getRmqModule().close();
            } catch (Exception e) {
                log.warn("Err Occurs", e);
            }
        }
        return rmqInfo;
    }

    /**
     * RmqConsumer를 사용하여 RMQ 매니저를 시작한다.
     *
     * @param rmqConsumer RMQ 소비자.
     */
/*    public void start(RmqConsumer rmqConsumer) {
        this.start(rmqConsumer::consume);
    }*/

    /**
     * Consumer<byte[]>를 사용하여 RMQ 매니저를 시작한다.
     *
     * @param rmqConsumer RMQ 소비자.
     */
    public void start(Consumer<byte[]> rmqConsumer) {
        if (!isRunning.compareAndSet(false, true)) {
            log.warn("RmqManager Already started");
            return;
        }

        for (Map.Entry<String, RmqInfo> entry : rmqInfos.entrySet()) {
            RmqInfo rmqInfo = entry.getValue();

            RmqModule rmqModule = rmqInfo.getRmqModule();
            String consumerQueue = rmqInfo.getConsumerQueue();
            Runnable onConnected = rmqInfo.getOnConnected();
            Runnable onDisconnected = rmqInfo.getOnDisconnected();
            // RmqInfo 에 설정된 RMQ 소비자가 있는 경우, 메시지 처리 시 해당 소비자를 사용
            RmqConsumer infoConsumer = rmqInfo.getRmqConsumer();

            rmqModule.connectWithAsyncRetry(() -> {
                try {
                    onConnected.run();
                } catch (Exception e) {
                    log.warn("Err Occurs onConnected", e);
                }

                try {
                    rmqModule.queueDeclare(consumerQueue);
                    if (infoConsumer != null || rmqConsumer != null)
                        rmqModule.registerByteConsumer(consumerQueue, (msg) -> {
                            try {
                                //log.info("Register RMQ Consumer (RmqInfoConsumer:{}/RmqConsumer:{})", infoConsumer != null, rmqConsumer != null);
                                if (infoConsumer != null) {
                                    Consumer<byte[]> consumer = infoConsumer::consume;
                                    consumer.accept(msg);
                                } else {
                                    rmqConsumer.accept(msg);
                                }
                            } catch (Exception e) {
                                log.warn("Err Occurs while handling RMQ message ", e);
                            }

                    });
                } catch (IOException e) {
                    log.warn("Fail to declare queue. [{}]", consumerQueue);
                }

            }, () -> {
                try {
                    onDisconnected.run();
                } catch (Exception e) {
                    log.warn("Err Occurs onDisconnected", e);
                }

            });
        }
    }

    /**
     * 모든 실행 중인 RabbitMQ 모듈을 멈추고 매니저를 초기화한다.
     */
    public void stop() {
        if (!isRunning.compareAndSet(true, false)) {
            log.warn("RmqManager Already stopped");
            return;
        }
        rmqInfos.keySet().forEach(this::removeRmqModule);
    }

    public RmqModule getRmqModule(String key) {
        return rmqInfos.get(key).getRmqModule();
    }

    public Map<String, RmqInfo> getCloneRmqModules() {
        return new HashMap<>(rmqInfos);
    }

    @Getter
    public static class RmqInfo {
        final RmqModule rmqModule;
        final String consumerQueue;
        final Runnable onConnected;
        final Runnable onDisconnected;
        RmqConsumer rmqConsumer;

        public RmqInfo(RmqModule rmqModule, String consumerQueue, Runnable onConnected, Runnable onDisconnected, RmqConsumer rmqConsumer) {
            this.rmqModule = rmqModule;
            this.consumerQueue = consumerQueue;
            this.onConnected = onConnected;
            this.onDisconnected = onDisconnected;
            this.rmqConsumer = rmqConsumer;
        }
    }
}
