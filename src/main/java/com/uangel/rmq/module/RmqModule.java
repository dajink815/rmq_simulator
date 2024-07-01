package com.uangel.rmq.module;

import com.rabbitmq.client.*;
import com.rabbitmq.client.impl.DefaultExceptionHandler;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Consumer;


/**
 * RabbitMQ와의 연결을 관리하며, 다양한 RabbitMQ 관련 작업을 제공하는 클래스.
 * 이 클래스는 다음과 같은 기능을 수행한다:
 * <ul>
 *   <li> RabbitMQ 서버와의 연결 설정 및 종료 </li>
 *   <li> 채널 생성 </li>
 *   <li> 메시지 큐 선언 </li>
 *   <li> 지정된 큐에 메시지 전송 </li>
 *   <li> 지정된 큐에 소비자 등록 </li>
 * </ul>
 */
@Slf4j
@Getter
public class RmqModule {
    private static final BasicThreadFactory SENDER_THREAD_FACTORY = new BasicThreadFactory.Builder().namingPattern("RMQ_SENDER_%d").daemon(true).build();
    private static final BasicThreadFactory RCVER_THREAD_FACTORY = new BasicThreadFactory.Builder().namingPattern("RMQ_RCVER_%d").daemon(true).build();

    // RabbitMQ 서버와의 연결을 재시도하는 간격(단위:ms)
    private static final int RECOVERY_INTERVAL = 1000;
    // RabbitMQ 서버에게 전송하는 heartbeat 요청의 간격(단위:sec)
    private static final int REQUESTED_HEARTBEAT = 5;
    // RabbitMQ 서버와 연결을 시도하는 최대 시간(단위:ms)
    private static final int CONNECTION_TIMEOUT = 2000;

    private final String host;
    private final String userName;
    private final String password;
    private final Integer port;
    private final Integer consumerCount;

    private final ArrayBlockingQueue<Runnable> sendQueue;
    private final ArrayBlockingQueue<Runnable> recvQueue;

    private ScheduledExecutorService rmqSender;
    private ScheduledExecutorService rmqReceiver;

    @Setter
    private int qos = 100;

    // RabbitMQ 서버와의 연결과 채널을 관리하기 위한 변수
    private Connection connection;
    private Channel channel;

    /**
     * @param host        RabbitMQ 서버의 호스트
     * @param userName    RabbitMQ 서버에 연결할 사용자 이름
     * @param password    해당 사용자의 비밀번호
     * @param port        RabbitMQ 서버 포트
     * @param bufferCount RMQ Send/Recv Buffer 크기
     */
    public RmqModule(String host, String userName, String password, Integer port, int consumerCount, int bufferCount) {
        this.host = host;
        this.userName = userName;
        this.password = password;
        this.port = port;
        this.consumerCount = (consumerCount <= 0)? 1 : consumerCount;
        this.sendQueue = new ArrayBlockingQueue<>(bufferCount);
        this.recvQueue = new ArrayBlockingQueue<>(bufferCount);
    }

    public RmqModule(String host, String userName, String password, int consumerCount, int bufferCount) {
        this(host, userName, password, null, consumerCount, bufferCount);
    }

    /**
     * RabbitMQ 서버에 연결을 수립하며 통신을 위한 채널을 생성하고, 연결에 대한 예외 처리기를 설정하고, 만약 연결이 복구 가능한 경우, 복구 리스너도 설정한다.
     * 연결과 채널이 성공적으로 수립되면 제공된 onConnected 콜백을, 연결에 실패하면 onDisconnected 콜백을 호출한다.
     *
     * @param onConnected    연결과 채널이 성공적으로 수립되었을 때 호출되는 콜백함수
     * @param onDisconnected 예기치 않은 연결 드라이버 예외가 발생했을 때 호출되는 콜백
     * @throws IOException      연결과 채널을 생성하는 동안 I/O 에러가 발생한 경우
     * @throws TimeoutException 연결과 채널을 생성하는 동안 타임아웃이 발생한 경우
     */
    public RmqModule connect(Runnable onConnected, Runnable onDisconnected) throws IOException, TimeoutException {
        if (isConnected()) {
            throw new IllegalStateException("RMQ Already Connected");
        }

        try {
            ConnectionFactory factory = new ConnectionFactory();

            // RabbitMQ 서버 정보 설정
            factory.setHost(host);
            factory.setUsername(userName);
            factory.setPassword(password);
            if (this.port != null) {
                factory.setPort(this.port);
            }

            // 자동 복구를 활성화하고, 네트워크 복구 간격, heartbeat 요청 간격, 연결 타임아웃 시간을 설정
            factory.setAutomaticRecoveryEnabled(true);
            factory.setNetworkRecoveryInterval(RECOVERY_INTERVAL);
            factory.setRequestedHeartbeat(REQUESTED_HEARTBEAT);
            factory.setConnectionTimeout(CONNECTION_TIMEOUT);

            factory.setExceptionHandler(new DefaultExceptionHandler() {
                @Override
                public void handleUnexpectedConnectionDriverException(Connection con, Throwable exception) {
                    super.handleUnexpectedConnectionDriverException(con, exception);
                    onDisconnected.run();
                }
            });

            // 연결과 채널 생성 시도
            this.connection = factory.newConnection();
            ((Recoverable) connection).addRecoveryListener(new RecoveryListener() {
                public void handleRecovery(Recoverable r) {
                    onConnected.run();
                }

                public void handleRecoveryStarted(Recoverable r) {
                    onDisconnected.run();
                }
            });

            this.channel = connection.createChannel();
            try {
                if (this.rmqSender != null && !this.rmqSender.isShutdown()) {
                    this.rmqSender.shutdown();
                }
            } catch (Exception e) {
                log.warn("Error while shutdown Scheduler.", e);
            }
            try {
                if (this.rmqReceiver != null && !this.rmqReceiver.isShutdown()) {
                    this.rmqReceiver.shutdown();
                }
            } catch (Exception e) {
                log.warn("Error while shutdown Scheduler.", e);
            }
            this.rmqSender = Executors.newSingleThreadScheduledExecutor(SENDER_THREAD_FACTORY);
            // todo server 역할 하는 RmqModule 일 때만 실행
            this.rmqReceiver = Executors.newScheduledThreadPool(consumerCount, RCVER_THREAD_FACTORY);

            this.rmqSender.scheduleWithFixedDelay(() -> {
                while (true) {
                    try {
                        Runnable runnable = sendQueue.poll();
                        if (runnable == null) {
                            return;
                        }
                        runnable.run();
                    } catch (Exception e) {
                        log.warn("Error Occurs", e);
                    }
                }
            }, 0, 10, TimeUnit.MILLISECONDS);

            rmqReceiver.scheduleWithFixedDelay(() -> {
                while (true) {
                    try {
                        Runnable runnable = recvQueue.poll();
                        if (runnable == null) {
                            return;
                        }
                        runnable.run();
                    } catch (Exception e) {
                        log.warn("Error Occurs", e);
                    }
                }
            }, 0, 10, TimeUnit.MILLISECONDS);

            channel.basicQos(qos);
            onConnected.run();
            return this;
        } catch (Exception e) {
            onDisconnected.run();
            throw e;
        }
    }

    public RmqModule connect() throws IOException, TimeoutException {
        return this.connect(() -> log.debug("RabbitMQ Connected [{}]", host), () -> log.debug("RabbitMQ Disconnected [{}]", host));
    }

    /**
     * 지정된 이름의 메시지 큐를 생성한다.
     *
     * @param queueName 생성할 큐의 이름
     * @throws IOException 큐 생성에 실패한 경우
     */
    public RmqModule queueDeclare(String queueName) throws IOException {
        this.queueDeclare(queueName, null);
        return this;
    }

    public RmqModule queueDeclare(String queueName, Map<String, Object> arguments) throws IOException {
        channel.queueDeclare(queueName, false, false, false, arguments);
        return this;
    }

    public RmqModule queueDeclare(String queue, boolean durable, boolean exclusive, boolean autoDelete, Map<String, Object> arguments) throws IOException {
        channel.queueDeclare(queue, durable, exclusive, autoDelete, arguments);
        return this;
    }

    /**
     * 지정된 이름의 큐에 메시지를 전송한다.
     *
     * @param queueName 전송할 큐의 이름
     * @param message   전송할 메시지
     */
    public RmqModule sendMessage(String queueName, String message) {
        return sendMessage(queueName, message.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 지정된 이름의 큐에 만료 시간과 함께 메시지를 전송한다.
     *
     * @param queueName  전송할 큐의 이름
     * @param message    전송할 메시지
     * @param expiration 메시지의 만료 시간
     */
    public RmqModule sendMessage(String queueName, String message, int expiration) {
        return sendMessage(queueName, message.getBytes(StandardCharsets.UTF_8), expiration);
    }


    /**
     * 지정된 이름의 큐에 바이트 배열 형태의 메시지를 전송한다.
     *
     * @param queueName 전송할 큐의 이름
     * @param message   전송할 메시지의 바이트 배열
     */
    public RmqModule sendMessage(String queueName, byte[] message) {
        return sendMessage(() -> {
            try {
                channel.basicPublish("", queueName, MessageProperties.PERSISTENT_TEXT_PLAIN, message);
            } catch (Exception e) {
                log.warn("Err Occurs", e);
            }
        });
    }

    /**
     * 지정된 이름의 큐에 만료 시간과 함께 바이트 배열 형태의 메시지를 전송한다.
     *
     * @param queueName  전송할 큐의 이름
     * @param message    전송할 메시지의 바이트 배열
     * @param expiration 메시지의 만료 시간
     */
    public RmqModule sendMessage(String queueName, byte[] message, int expiration) {
        return sendMessage(() -> {
            try {
                AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder().expiration(Integer.toString(expiration)).build();
                channel.basicPublish("", queueName, properties, message);
            } catch (Exception e) {
                log.warn("Err Occurs", e);
            }
        });
    }

    private RmqModule sendMessage(Runnable runnable) {
        if (!this.sendQueue.offer(runnable)) {
            log.warn("RMQ SND Queue full. Drop message.");
        }
        return this;
    }

    /**
     * 지정된 큐에 소비자를 등록한다.
     *
     * @param queueName       소비자를 등록할 큐의 이름
     * @param deliverCallback 메시지가 수신될 때 호출되는 콜백
     * @throws IOException 소비자 등록에 실패한 경우
     */
    @Synchronized
    public RmqModule registerConsumer(String queueName, DeliverCallback deliverCallback, Map<String, Object> arguments) throws IOException {
        channel.basicConsume(queueName, true, arguments, (s, delivery) -> recvQueue.add(() -> {
            try {
                deliverCallback.handle(s, delivery);
            } catch (Exception e) {
                log.warn("Error while handling message", e);
            }
        }), cancelCallback -> {
        });
        return this;
    }

    /**
     * 지정된 큐에 바이트 배열 형태의 메시지를 처리할 소비자를 등록한다.
     * 주로 바이너리 데이터를 받고 처리하는 경우에 사용한다.
     *
     * @param queueName   소비자를 등록할 큐의 이름
     * @param msgCallback 메시지가 수신될 때 호출되는 콜백, 메시지는 바이트 배열로 전달됨
     * @throws IOException 소비자 등록에 실패한 경우
     */
    public RmqModule registerConsumer(String queueName, Consumer<byte[]> msgCallback) throws IOException {
        return this.registerConsumer(queueName, (s, delivery) -> msgCallback.accept(delivery.getBody()), null);
    }

    public boolean isConnected() {
        return this.channel != null && this.channel.isOpen() &&
                this.connection != null && this.connection.isOpen();
    }


    /**
     * RabbitMQ 서버와의 연결 및 채널을 종료한다.
     * 연결이나 채널 종료 과정에서 오류가 발생하면 로그에 출력한다.
     */
    @Synchronized
    public RmqModule close() {
        try {
            if (this.channel != null && this.channel.isOpen()) {
                this.channel.close();
            }
        } catch (Exception e) {
            log.warn("Error while closing the channel", e);
        }

        try {
            if (this.connection != null && this.connection.isOpen()) {
                this.connection.close();
            }
        } catch (Exception e) {
            log.warn("Error while closing the connection", e);
        }

        try {
            if (this.rmqSender != null && !this.rmqSender.isShutdown()) {
                this.rmqSender.shutdown();
            }
        } catch (Exception e) {
            log.warn("Error while shutdown Scheduler.", e);
        }
        log.info("RMQ Module Closed");
        return this;
    }

    //////////////////////////////////
    // !! RMQ Stream 전용 메서드 !! //
    //////////////////////////////////

    /**
     * 지정된 이름의 메시지 스트림 큐를 생성한다.
     *
     * @param queueName                 생성할 큐의 이름
     * @param maxAge                    메시지 수명. 가능한 단위: Y, M, D, h, m, s. (e.g. 7D = 일주일)
     * @param maxLengthBytes            큐의 최대 총 크기(바이트)
     * @param streamMaxSegmentSizeBytes 스트림의 최대 세그먼트 크기(바이트)
     * @throws IOException 큐 생성에 실패한 경우
     */
    public RmqModule queueDeclareAsStream(String queueName, String maxAge, Long maxLengthBytes, Long streamMaxSegmentSizeBytes) throws IOException {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-queue-type", "stream");
        if (maxAge != null) {
            arguments.put("x-max-age", maxAge);
        }
        if (maxLengthBytes != null) {
            arguments.put("x-max-length-bytes", maxLengthBytes);
        }
        if (streamMaxSegmentSizeBytes != null) {
            arguments.put("x-stream-max-segment-size-bytes", streamMaxSegmentSizeBytes);
        }
        this.queueDeclare(queueName, true, false, false, arguments);
        return this;
    }

    public RmqModule queueDeclareAsStream(String queueName) throws IOException {
        return queueDeclareAsStream(queueName, null, null, null);
    }

    /**
     * @param streamOffset "first" - 먼저 스트림에서 사용 가능한 첫 번째 메시지부터 소비를 시작
     *                     "last" - 마지막으로 작성된 메시지 덩어리에서 소비를 시작
     *                     "next" - 스트림 끝부터 소비를 시작
     *                     Integer - 특정 오프셋에서 시작
     *                     Date - 주어진 시간부터 시작
     *                     null - "next"와 동일
     */
    @Synchronized
    public RmqModule registerConsumerAsStream(String queueName, DeliverCallback deliverCallback, @NonNull Object streamOffset) throws IOException {
        channel.basicConsume(queueName, false, new HashMap<String, Object>() {{
            put("x-stream-offset", streamOffset);
        }}, (s, delivery) -> recvQueue.add(() -> {
            try {
                deliverCallback.handle(s, delivery);
            } catch (Exception e) {
                log.warn("Error while handling message", e);
            }
        }), consumerTag -> {
        });
        return this;
    }

    public RmqModule registerConsumerAsStream(String queueName, Consumer<byte[]> msgCallback) throws IOException {
        return registerConsumerAsStream(queueName, (s, delivery) -> msgCallback.accept(delivery.getBody()), "next");
    }
}

